package com.mss301.brand.service;

import com.mss301.brand.dto.AssignmentResponse;
import com.mss301.brand.dto.BrandResponse;
import com.mss301.brand.dto.BrandWatchesResponse;
import com.mss301.brand.entity.BrandWatch;
import com.mss301.brand.entity.WatchBrand;
import com.mss301.brand.repository.BrandRepository;
import com.mss301.brand.repository.BrandWatchRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.server.ResponseStatusException;

import java.time.LocalDateTime;
import java.util.List;

@Service
@RequiredArgsConstructor
@Slf4j
public class BrandService {

    private final BrandRepository brandRepository;
    private final BrandWatchRepository brandWatchRepository;

    public List<BrandResponse> findAll() {
        return brandRepository.findAll().stream()
                .map(this::toResponse)
                .toList();
    }

    public BrandResponse findById(Integer brandId) {
        return toResponse(getBrand(brandId));
    }

    public BrandWatchesResponse watchesOf(Integer brandId) {
        WatchBrand brand = getBrand(brandId);
        List<Integer> watchIds = brandWatchRepository.findByBrandIdOrderByWatchIdAsc(brandId).stream()
                .map(BrandWatch::getWatchId)
                .toList();
        return new BrandWatchesResponse(brandId, brand.getBrandName(), watchIds.size(), watchIds);
    }

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

    @Transactional
    public AssignmentResponse unassignWatch(Integer watchId) {
        return brandWatchRepository.findByWatchId(watchId)
                .map(existing -> {
                    Integer previousBrandId = existing.getBrandId();
                    brandWatchRepository.delete(existing);
                    log.info(
                            "[SAGA-PARTICIPANT] UNASSIGN watch={} from brand={}",
                            watchId,
                            previousBrandId);
                    return new AssignmentResponse(watchId, null, previousBrandId, "UNASSIGNED");
                })
                .orElseGet(() -> {
                    log.info(
                            "[SAGA-PARTICIPANT] UNASSIGN watch={} -> nothing to do (idempotent)",
                            watchId);
                    return new AssignmentResponse(watchId, null, null, "NOTHING_TO_DO");
                });
    }

    private WatchBrand getBrand(Integer brandId) {
        return brandRepository.findById(brandId)
                .orElseThrow(() -> new ResponseStatusException(
                        HttpStatus.NOT_FOUND,
                        "Brand not found with id: " + brandId));
    }

    private BrandResponse toResponse(WatchBrand brand) {
        return BrandResponse.builder()
                .brandId(brand.getBrandId())
                .brandName(brand.getBrandName())
                .countryOfOrigin(brand.getCountryOfOrigin())
                .watchCount(brandWatchRepository.countByBrandId(brand.getBrandId()))
                .build();
    }
}
