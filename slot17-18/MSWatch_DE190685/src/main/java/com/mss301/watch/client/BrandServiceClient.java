package com.mss301.watch.client;

import org.springframework.cloud.openfeign.FeignClient;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PutMapping;

@FeignClient(name = "ms-brand", url = "${brand-service.url}")
public interface BrandServiceClient {

    @PutMapping("/internal/brands/{brandId}/watches/{watchId}")
    void assignWatch(@PathVariable("brandId") Integer brandId, @PathVariable("watchId") Integer watchId);

    @DeleteMapping("/internal/brands/watches/{watchId}")
    void unassignWatch(@PathVariable("watchId") Integer watchId);
}
