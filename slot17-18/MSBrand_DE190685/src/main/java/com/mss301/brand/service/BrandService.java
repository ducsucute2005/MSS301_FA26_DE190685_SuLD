package com.mss301.brand.service;

import com.mss301.brand.dto.AssignmentResponse;
import com.mss301.brand.entity.BrandWatch;
import com.mss301.brand.repository.BrandRepository;
import com.mss301.brand.repository.BrandWatchRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.server.ResponseStatusException;

import java.time.LocalDateTime;

@Service
@RequiredArgsConstructor
@Slf4j
public class BrandService {

    private final BrandRepository brandRepository;
    private final BrandWatchRepository brandWatchRepository;

    @Transactional
    public AssignmentResponse assignWatch(Integer brandId, Integer watchId) {
        if (!brandRepository.existsById(brandId)) {
            throw new ResponseStatusException(
                    HttpStatus.NOT_FOUND,
                    "Brand not found with id: " + brandId);
        }

        BrandWatch link = brandWatchRepository.findByWatchId(watchId)
                .orElseGet(() -> BrandWatch.builder().watchId(watchId).build());
        Integer previousBrandId = link.getBrandId();

        link.setBrandId(brandId);
        link.setUpdatedAt(LocalDateTime.now());
        brandWatchRepository.save(link);

        log.info(
                "[SAGA-PARTICIPANT] ASSIGN watch={} -> brand={} (previous brand={})",
                watchId,
                brandId,
                previousBrandId);
        return new AssignmentResponse(watchId, brandId, previousBrandId, "ASSIGNED");
    }
}
