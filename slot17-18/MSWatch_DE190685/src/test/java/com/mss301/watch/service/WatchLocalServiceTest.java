package com.mss301.watch.service;

import com.mss301.watch.entity.Watch;
import com.mss301.watch.entity.WatchCategory;
import com.mss301.watch.repository.WatchCategoryRepository;
import com.mss301.watch.repository.WatchRepository;
import org.junit.jupiter.api.Test;
import org.springframework.http.HttpStatus;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.server.ResponseStatusException;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.util.Optional;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertSame;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

class WatchLocalServiceTest {

    private final WatchRepository watchRepository = mock(WatchRepository.class);
    private final WatchCategoryRepository categoryRepository = mock(WatchCategoryRepository.class);
    private final WatchLocalService service = new WatchLocalService(watchRepository, categoryRepository);

    @Test
    void loadReturnsSnapshot() {
        WatchCategory category = WatchCategory.builder().categoryId(3).build();
        Watch watch = sampleWatch(category);
        when(watchRepository.findById(7)).thenReturn(Optional.of(watch));

        WatchData result = service.load(7);

        assertEquals(7, result.watchId());
        assertEquals(3, result.categoryId());
        assertEquals(5, result.brandId());
        assertEquals("Test Watch", result.name());
    }

    @Test
    void loadMissingWatchReturnsNotFound() {
        when(watchRepository.findById(7)).thenReturn(Optional.empty());

        ResponseStatusException exception = assertThrows(ResponseStatusException.class, () -> service.load(7));

        assertEquals(HttpStatus.NOT_FOUND, exception.getStatusCode());
    }

    @Test
    void insertMapsSnapshotAndReturnsGeneratedId() {
        WatchCategory category = WatchCategory.builder().categoryId(3).build();
        when(categoryRepository.getReferenceById(3)).thenReturn(category);
        when(watchRepository.save(any(Watch.class))).thenAnswer(invocation -> {
            Watch saved = invocation.getArgument(0);
            saved.setWatchId(11);
            return saved;
        });

        Integer id = service.insert(sampleData(null));

        assertEquals(11, id);
        verify(watchRepository).save(any(Watch.class));
        verify(categoryRepository).getReferenceById(3);
    }

    @Test
    void updateAppliesSnapshotToExistingWatch() {
        Watch existing = sampleWatch(null);
        WatchCategory category = WatchCategory.builder().categoryId(3).build();
        when(watchRepository.findById(7)).thenReturn(Optional.of(existing));
        when(categoryRepository.getReferenceById(3)).thenReturn(category);

        service.update(sampleData(7));

        assertEquals("Updated Watch", existing.getName());
        assertEquals(5, existing.getBrandId());
        assertSame(category, existing.getCategory());
        assertEquals(new BigDecimal("125.00"), existing.getPrice());
        verify(watchRepository, never()).save(any(Watch.class));
    }

    @Test
    void updateMissingWatchThrows() {
        when(watchRepository.findById(7)).thenReturn(Optional.empty());

        assertThrows(IllegalStateException.class, () -> service.update(sampleData(7)));
    }

    @Test
    void deleteIfExistsIsIdempotentForMissingId() {
        when(watchRepository.existsById(7)).thenReturn(false);

        service.deleteIfExists(7);
        service.deleteIfExists(null);

        verify(watchRepository, never()).deleteById(7);
    }

    @Test
    void deleteIfExistsDeletesPresentWatch() {
        when(watchRepository.existsById(7)).thenReturn(true);

        service.deleteIfExists(7);

        verify(watchRepository).deleteById(7);
    }

    @Test
    void deleteRemovesExistingWatch() {
        Watch watch = sampleWatch(null);
        when(watchRepository.findById(7)).thenReturn(Optional.of(watch));

        service.delete(7);

        verify(watchRepository).delete(watch);
    }

    @Test
    void localDatabaseOperationsHaveSeparateTransactions() throws NoSuchMethodException {
        for (String method : new String[] {"insert", "update", "delete", "deleteIfExists"}) {
            Class<?>[] parameters = switch (method) {
                case "insert" -> new Class<?>[] {WatchData.class};
                case "update" -> new Class<?>[] {WatchData.class};
                default -> new Class<?>[] {Integer.class};
            };
            Transactional transaction = WatchLocalService.class.getMethod(method, parameters)
                .getAnnotation(Transactional.class);
            assertNotNull(transaction, method);
        }

        Transactional loadTransaction = WatchLocalService.class.getMethod("load", Integer.class)
            .getAnnotation(Transactional.class);
        assertNotNull(loadTransaction);
        assertEquals(true, loadTransaction.readOnly());
    }

    private Watch sampleWatch(WatchCategory category) {
        return Watch.builder()
            .watchId(7)
            .name("Test Watch")
            .category(category)
            .brandId(5)
            .caseMaterial("Steel")
            .strapMaterial("Leather")
            .waterpMaterial("50 meters")
            .price(new BigDecimal("125.00"))
            .releaseDate(LocalDate.of(2026, 1, 1))
            .stock(10)
            .build();
    }

    private WatchData sampleData(Integer watchId) {
        return new WatchData(
            watchId,
            "Updated Watch",
            3,
            5,
            "Steel",
            "Leather",
            "50 meters",
            new BigDecimal("125.00"),
            LocalDate.of(2026, 1, 1),
            10
        );
    }
}
