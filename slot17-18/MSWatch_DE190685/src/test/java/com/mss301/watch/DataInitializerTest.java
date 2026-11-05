package com.mss301.watch;

import com.mss301.watch.entity.Watch;
import com.mss301.watch.entity.WatchCategory;
import com.mss301.watch.repository.WatchCategoryRepository;
import com.mss301.watch.repository.WatchRepository;
import org.junit.jupiter.api.Test;

import java.util.List;
import java.util.concurrent.atomic.AtomicLong;
import java.util.concurrent.atomic.AtomicReference;
import java.util.stream.StreamSupport;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertSame;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

class DataInitializerTest {

    @Test
    void seedsRequiredCategoriesAndWatchesOnlyOnce() {
        WatchRepository watchRepository = mock(WatchRepository.class);
        WatchCategoryRepository categoryRepository = mock(WatchCategoryRepository.class);
        AtomicLong watchCount = new AtomicLong();
        AtomicLong categoryCount = new AtomicLong();
        AtomicReference<List<Watch>> seededWatches = new AtomicReference<>();
        AtomicReference<List<WatchCategory>> seededCategories = new AtomicReference<>();

        when(watchRepository.count()).thenAnswer(invocation -> watchCount.get());
        when(categoryRepository.count()).thenAnswer(invocation -> categoryCount.get());
        when(categoryRepository.saveAll(any())).thenAnswer(invocation -> {
            List<WatchCategory> categories = StreamSupport.stream(
                ((Iterable<WatchCategory>) invocation.getArgument(0)).spliterator(), false
            ).toList();
            seededCategories.set(categories);
            categoryCount.set(categories.size());
            return categories;
        });
        when(watchRepository.saveAll(any())).thenAnswer(invocation -> {
            List<Watch> watches = StreamSupport.stream(
                ((Iterable<Watch>) invocation.getArgument(0)).spliterator(), false
            ).toList();
            seededWatches.set(watches);
            watchCount.set(watches.size());
            return watches;
        });

        DataInitializer initializer = new DataInitializer(watchRepository, categoryRepository);
        initializer.run();
        initializer.run();

        assertEquals(5, seededCategories.get().size());
        assertEquals(List.of(
            "Dive Watches", "Chronograph Watches", "Dress Watches", "Digital Watches", "Pilot Watches"
        ), seededCategories.get().stream().map(WatchCategory::getCategoryName).toList());
        assertEquals(10, seededWatches.get().size());
        assertEquals("Submariner Date", seededWatches.get().get(0).getName());
        assertEquals("IWC Pilot's Watch Mark XVIII", seededWatches.get().get(9).getName());
        assertEquals(1, seededWatches.get().get(0).getBrandId());
        assertEquals(5, seededWatches.get().get(4).getBrandId());
        assertSame(seededCategories.get().get(0), seededWatches.get().get(0).getCategory());
        verify(categoryRepository).saveAll(any());
        verify(watchRepository).saveAll(any());
    }
}
