package com.mss301.brand;

import com.mss301.brand.entity.BrandWatch;
import com.mss301.brand.entity.WatchBrand;
import com.mss301.brand.repository.BrandRepository;
import com.mss301.brand.repository.BrandWatchRepository;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.ArgumentCaptor;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
class DataInitializerTests {

    @Mock
    private BrandRepository brandRepository;

    @Mock
    private BrandWatchRepository brandWatchRepository;

    @InjectMocks
    private DataInitializer dataInitializer;

    @Test
    void seedsRequiredBrandsAndWatchAssignmentsWhenTablesAreEmpty() {
        when(brandRepository.count()).thenReturn(0L);
        when(brandWatchRepository.count()).thenReturn(0L);

        dataInitializer.run();

        ArgumentCaptor<List<WatchBrand>> brandsCaptor = ArgumentCaptor.forClass(List.class);
        verify(brandRepository).saveAll(brandsCaptor.capture());
        assertEquals(
                List.of(
                        "Rolex:Switzerland",
                        "Omega:Switzerland",
                        "Seiko:Japan",
                        "Casio:Japan",
                        "TAG Heuer:Switzerland"),
                brandsCaptor.getValue().stream()
                        .map(brand -> brand.getBrandName() + ":" + brand.getCountryOfOrigin())
                        .toList());

        ArgumentCaptor<List<BrandWatch>> linksCaptor = ArgumentCaptor.forClass(List.class);
        verify(brandWatchRepository).saveAll(linksCaptor.capture());
        Map<Integer, Integer> assignments = linksCaptor.getValue().stream()
                .collect(Collectors.toMap(BrandWatch::getWatchId, BrandWatch::getBrandId));
        assertEquals(Map.of(
                1, 1, 2, 2, 3, 3, 4, 4, 5, 5,
                6, 2, 7, 3, 8, 1, 9, 4, 10, 4), assignments);
        assertEquals(10, linksCaptor.getValue().size());
        assertEquals(2, assignments.values().stream().filter(brandId -> brandId == 1).count());
        assertEquals(2, assignments.values().stream().filter(brandId -> brandId == 2).count());
        assertEquals(2, assignments.values().stream().filter(brandId -> brandId == 3).count());
        assertEquals(3, assignments.values().stream().filter(brandId -> brandId == 4).count());
        assertEquals(1, assignments.values().stream().filter(brandId -> brandId == 5).count());
        linksCaptor.getValue().forEach(link -> assertNotNull(link.getUpdatedAt()));
    }

    @Test
    void doesNotSeedTablesThatAlreadyContainData() {
        when(brandRepository.count()).thenReturn(5L);
        when(brandWatchRepository.count()).thenReturn(10L);

        dataInitializer.run();

        verify(brandRepository, never()).saveAll(org.mockito.ArgumentMatchers.any());
        verify(brandWatchRepository, never()).saveAll(org.mockito.ArgumentMatchers.any());
    }
}
