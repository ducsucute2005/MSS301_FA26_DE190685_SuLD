package com.mss301.watch.service;

import com.mss301.watch.dto.WatchRequest;
import com.mss301.watch.dto.WatchResponse;
import com.mss301.watch.entity.Watch;
import com.mss301.watch.entity.WatchCategory;
import com.mss301.watch.repository.WatchCategoryRepository;
import com.mss301.watch.repository.WatchRepository;
import com.mss301.watch.service.saga.SagaOutcome;
import com.mss301.watch.service.saga.SagaStatus;
import com.mss301.watch.service.saga.WatchSagaOrchestrator;
import org.junit.jupiter.api.Test;
import org.springframework.http.HttpStatus;
import org.springframework.web.server.ResponseStatusException;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.util.List;
import java.util.Optional;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNull;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

class WatchServiceTest {

    private final WatchRepository watchRepository = mock(WatchRepository.class);
    private final WatchCategoryRepository categoryRepository = mock(WatchCategoryRepository.class);
    private final WatchLocalService localService = mock(WatchLocalService.class);
    private final WatchSagaOrchestrator orchestrator = mock(WatchSagaOrchestrator.class);
    private final WatchService service = new WatchService(
        watchRepository, categoryRepository, localService, orchestrator
    );

    @Test
    void findAllReturnsWatchesInRepositoryOrderWithCategoryName() {
        WatchCategory category = WatchCategory.builder().categoryId(3).categoryName("Dress Watches").build();
        Watch newer = Watch.builder().watchId(10).name("Newest Watch").category(category).build();
        Watch older = Watch.builder().watchId(4).name("Older Watch").category(category).build();
        when(watchRepository.findAllByOrderByWatchIdDesc()).thenReturn(List.of(newer, older));

        List<WatchResponse> result = service.findAll();

        assertEquals(List.of(10, 4), result.stream().map(WatchResponse::watchId).toList());
        assertEquals("Dress Watches", result.get(0).categoryName());
    }

    @Test
    void createValidatesBeforeStartingSagaAndReturnsSavedWatch() {
        WatchRequest request = validRequest();
        WatchCategory category = WatchCategory.builder().categoryId(2).categoryName("Chronograph").build();
        Watch saved = Watch.builder().watchId(22).name(request.getName()).category(category)
            .brandId(request.getBrandId()).price(request.getPrice()).releaseDate(request.getReleaseDate())
            .stock(request.getStock()).build();
        when(categoryRepository.existsById(2)).thenReturn(true);
        when(orchestrator.createWatch(any(WatchData.class), org.mockito.ArgumentMatchers.isNull()))
            .thenReturn(new SagaOutcome("saga-22", 22, SagaStatus.COMPLETED));
        when(watchRepository.findById(22)).thenReturn(Optional.of(saved));

        SagaResult<WatchResponse> result = service.create(request, null);

        assertEquals("saga-22", result.sagaId());
        assertEquals(22, result.body().watchId());
        assertEquals("Chronograph", result.body().categoryName());
        verify(orchestrator).createWatch(any(WatchData.class), org.mockito.ArgumentMatchers.isNull());
    }

    @Test
    void createRejectsInvalidReleaseDateBeforeStartingSaga() {
        WatchRequest request = validRequest();
        request.setReleaseDate(LocalDate.now().minusDays(1));

        ResponseStatusException exception = assertThrows(ResponseStatusException.class,
            () -> service.create(request, null));

        assertEquals(HttpStatus.BAD_REQUEST, exception.getStatusCode());
        verify(orchestrator, never()).createWatch(any(), any());
    }

    @Test
    void createRejectsUnknownCategoryBeforeStartingSaga() {
        WatchRequest request = validRequest();
        when(categoryRepository.existsById(request.getCategoryId())).thenReturn(false);

        ResponseStatusException exception = assertThrows(ResponseStatusException.class,
            () -> service.create(request, null));

        assertEquals(HttpStatus.BAD_REQUEST, exception.getStatusCode());
        verify(orchestrator, never()).createWatch(any(), any());
    }

    @Test
    void createRejectsMissingReleaseDateBeforeStartingSaga() {
        WatchRequest request = validRequest();
        request.setReleaseDate(null);

        assertThrows(ResponseStatusException.class, () -> service.create(request, null));

        verify(categoryRepository, never()).existsById(any());
        verify(orchestrator, never()).createWatch(any(), any());
    }

    private WatchRequest validRequest() {
        WatchRequest request = new WatchRequest();
        request.setName("A Valid Watch Name");
        request.setCategoryId(2);
        request.setBrandId(5);
        request.setCaseMaterial("Steel");
        request.setStrapMaterial("Leather");
        request.setWaterpMaterial("50 meters");
        request.setPrice(new BigDecimal("125.00"));
        request.setReleaseDate(LocalDate.now());
        request.setStock(10);
        return request;
    }
}
