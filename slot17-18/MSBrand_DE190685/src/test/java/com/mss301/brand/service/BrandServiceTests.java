package com.mss301.brand.service;

import com.mss301.brand.dto.AssignmentResponse;
import com.mss301.brand.dto.BrandResponse;
import com.mss301.brand.dto.BrandWatchesResponse;
import com.mss301.brand.entity.BrandWatch;
import com.mss301.brand.entity.WatchBrand;
import com.mss301.brand.repository.BrandRepository;
import com.mss301.brand.repository.BrandWatchRepository;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.http.HttpStatus;
import org.springframework.web.server.ResponseStatusException;

import java.util.Optional;
import java.util.List;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertNull;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
class BrandServiceTests {

    @Mock
    private BrandRepository brandRepository;

    @Mock
    private BrandWatchRepository brandWatchRepository;

    @InjectMocks
    private BrandService brandService;

    @Test
    void assignsWatchToExistingBrand() {
        when(brandRepository.existsById(1)).thenReturn(true);
        when(brandWatchRepository.findByWatchId(99)).thenReturn(Optional.empty());
        when(brandWatchRepository.save(org.mockito.ArgumentMatchers.any(BrandWatch.class)))
                .thenAnswer(invocation -> invocation.getArgument(0));

        AssignmentResponse response = brandService.assignWatch(1, 99);

        assertEquals(new AssignmentResponse(99, 1, null, "ASSIGNED"), response);
        verify(brandWatchRepository).save(org.mockito.ArgumentMatchers.argThat(link ->
                link.getWatchId().equals(99)
                        && link.getBrandId().equals(1)
                        && link.getUpdatedAt() != null));
    }

    @Test
    void updatesExistingWatchAssignment() {
        BrandWatch existing = BrandWatch.builder()
                .id(7L)
                .watchId(99)
                .brandId(2)
                .build();
        when(brandRepository.existsById(1)).thenReturn(true);
        when(brandWatchRepository.findByWatchId(99)).thenReturn(Optional.of(existing));
        when(brandWatchRepository.save(existing)).thenReturn(existing);

        AssignmentResponse response = brandService.assignWatch(1, 99);

        assertEquals(new AssignmentResponse(99, 1, 2, "ASSIGNED"), response);
        assertEquals(1, existing.getBrandId());
        assertNotNull(existing.getUpdatedAt());
        verify(brandWatchRepository).save(existing);
    }

    @Test
    void repeatedAssignmentLeavesWatchAssignedToSameBrand() {
        BrandWatch existing = BrandWatch.builder()
                .id(7L)
                .watchId(99)
                .brandId(1)
                .build();
        when(brandRepository.existsById(1)).thenReturn(true);
        when(brandWatchRepository.findByWatchId(99))
                .thenReturn(Optional.of(existing), Optional.of(existing));
        when(brandWatchRepository.save(existing)).thenReturn(existing);

        AssignmentResponse first = brandService.assignWatch(1, 99);
        AssignmentResponse second = brandService.assignWatch(1, 99);

        assertEquals(1, existing.getBrandId());
        assertEquals(1, first.brandId());
        assertEquals(1, second.brandId());
        verify(brandWatchRepository, org.mockito.Mockito.times(2)).save(existing);
    }

    @Test
    void rejectsAssignmentToUnknownBrand() {
        when(brandRepository.existsById(999)).thenReturn(false);

        ResponseStatusException exception = assertThrows(
                ResponseStatusException.class,
                () -> brandService.assignWatch(999, 99));

        assertEquals(HttpStatus.NOT_FOUND, exception.getStatusCode());
        assertEquals("Brand not found with id: 999", exception.getReason());
        verify(brandWatchRepository, never()).findByWatchId(99);
        verify(brandWatchRepository, never()).save(org.mockito.ArgumentMatchers.any());
    }

    @Test
    void unassignsWatchAndReturnsPreviousBrand() {
        BrandWatch existing = BrandWatch.builder()
                .id(7L)
                .watchId(99)
                .brandId(2)
                .build();
        when(brandWatchRepository.findByWatchId(99)).thenReturn(Optional.of(existing));

        AssignmentResponse response = brandService.unassignWatch(99);

        assertEquals(new AssignmentResponse(99, null, 2, "UNASSIGNED"), response);
        verify(brandWatchRepository).delete(existing);
    }

    @Test
    void unassignsMissingWatchAsNothingToDo() {
        when(brandWatchRepository.findByWatchId(99)).thenReturn(Optional.empty());

        AssignmentResponse response = brandService.unassignWatch(99);

        assertEquals(new AssignmentResponse(99, null, null, "NOTHING_TO_DO"), response);
        verify(brandWatchRepository, never()).delete(org.mockito.ArgumentMatchers.any());
    }

    @Test
    void repeatedUnassignIsIdempotent() {
        BrandWatch existing = BrandWatch.builder()
                .id(7L)
                .watchId(99)
                .brandId(2)
                .build();
        when(brandWatchRepository.findByWatchId(99))
                .thenReturn(Optional.of(existing), Optional.empty());

        AssignmentResponse first = brandService.unassignWatch(99);
        AssignmentResponse second = brandService.unassignWatch(99);

        assertEquals("UNASSIGNED", first.status());
        assertEquals(2, first.previousBrandId());
        assertEquals("NOTHING_TO_DO", second.status());
        assertNull(second.previousBrandId());
        verify(brandWatchRepository).delete(existing);
    }

    @Test
    void findAllIncludesWatchCountForEachBrand() {
        WatchBrand rolex = WatchBrand.builder()
                .brandId(1)
                .brandName("Rolex")
                .countryOfOrigin("Switzerland")
                .build();
        when(brandRepository.findAll()).thenReturn(List.of(rolex));
        when(brandWatchRepository.countByBrandId(1)).thenReturn(2L);

        List<BrandResponse> brands = brandService.findAll();

        assertEquals(List.of(new BrandResponse(1, "Rolex", "Switzerland", 2)), brands);
    }

    @Test
    void findByIdReturnsBrandAndWatchCount() {
        WatchBrand rolex = WatchBrand.builder()
                .brandId(1)
                .brandName("Rolex")
                .countryOfOrigin("Switzerland")
                .build();
        when(brandRepository.findById(1)).thenReturn(Optional.of(rolex));
        when(brandWatchRepository.countByBrandId(1)).thenReturn(2L);

        BrandResponse response = brandService.findById(1);

        assertEquals(new BrandResponse(1, "Rolex", "Switzerland", 2), response);
    }

    @Test
    void watchesOfReturnsSortedWatchIdsAndCount() {
        WatchBrand rolex = WatchBrand.builder()
                .brandId(1)
                .brandName("Rolex")
                .countryOfOrigin("Switzerland")
                .build();
        when(brandRepository.findById(1)).thenReturn(Optional.of(rolex));
        when(brandWatchRepository.findByBrandIdOrderByWatchIdAsc(1)).thenReturn(List.of(
                BrandWatch.builder().watchId(1).brandId(1).build(),
                BrandWatch.builder().watchId(8).brandId(1).build()));

        BrandWatchesResponse response = brandService.watchesOf(1);

        assertEquals(new BrandWatchesResponse(1, "Rolex", 2, List.of(1, 8)), response);
    }

    @Test
    void findByIdRejectsUnknownBrand() {
        when(brandRepository.findById(999)).thenReturn(Optional.empty());

        ResponseStatusException exception = assertThrows(
                ResponseStatusException.class,
                () -> brandService.findById(999));

        assertEquals(HttpStatus.NOT_FOUND, exception.getStatusCode());
    }
}
