package com.mss301.brand.dto;

import lombok.Builder;

@Builder
public record BrandResponse(
        Integer brandId,
        String brandName,
        String countryOfOrigin,
        long watchCount) {
}
