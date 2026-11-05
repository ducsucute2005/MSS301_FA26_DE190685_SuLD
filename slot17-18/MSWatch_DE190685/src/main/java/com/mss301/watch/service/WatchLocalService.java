package com.mss301.watch.service;

import com.mss301.watch.entity.Watch;
import com.mss301.watch.repository.WatchCategoryRepository;
import com.mss301.watch.repository.WatchRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.server.ResponseStatusException;

@Service
@RequiredArgsConstructor
@Slf4j
public class WatchLocalService {

    private final WatchRepository watchRepository;
    private final WatchCategoryRepository categoryRepository;

    @Transactional(readOnly = true)
    public WatchData load(Integer id) {
        return watchRepository.findById(id)
            .map(WatchData::from)
            .orElseThrow(() -> new ResponseStatusException(
                HttpStatus.NOT_FOUND, "Watch not found with id: " + id
            ));
    }

    @Transactional
    public Integer insert(WatchData data) {
        Watch watch = new Watch();
        apply(watch, data);
        Integer id = watchRepository.save(watch).getWatchId();
        log.info("[LOCAL TX] INSERT watch id={} brand={}", id, data.brandId());
        return id;
    }

    @Transactional
    public void update(WatchData data) {
        Watch watch = watchRepository.findById(data.watchId())
            .orElseThrow(() -> new IllegalStateException(
                "Watch " + data.watchId() + " no longer exists"
            ));
        apply(watch, data);
        log.info("[LOCAL TX] UPDATE watch id={} brand={} name='{}'",
            data.watchId(), data.brandId(), data.name());
    }

    @Transactional
    public void deleteIfExists(Integer id) {
        if (id != null && watchRepository.existsById(id)) {
            watchRepository.deleteById(id);
            log.info("[LOCAL TX] DELETE watch id={} (compensation)", id);
        }
    }

    @Transactional
    public void delete(Integer id) {
        Watch watch = watchRepository.findById(id)
            .orElseThrow(() -> new IllegalStateException("Watch " + id + " no longer exists"));
        watchRepository.delete(watch);
        log.info("[LOCAL TX] DELETE watch id={}", id);
    }

    private void apply(Watch watch, WatchData data) {
        watch.setName(data.name());
        watch.setCategory(categoryRepository.getReferenceById(data.categoryId()));
        watch.setBrandId(data.brandId());
        watch.setCaseMaterial(data.caseMaterial());
        watch.setStrapMaterial(data.strapMaterial());
        watch.setWaterpMaterial(data.waterpMaterial());
        watch.setPrice(data.price());
        watch.setReleaseDate(data.releaseDate());
        watch.setStock(data.stock());
    }
}
