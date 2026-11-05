package com.mss301.brand.service;

import com.mss301.brand.dto.AssignmentResponse;
import com.mss301.brand.entity.BrandWatch;
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

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotNull;
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
}
