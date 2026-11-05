package com.mss301.watch.dto;

import java.math.BigDecimal;
import java.time.LocalDate;

public record WatchResponse(
    Integer watchId,
    String name,
    Integer categoryId,
    String categoryName,
    Integer brandId,
    String caseMaterial,
    String strapMaterial,
    String waterpMaterial,
    BigDecimal price,
    LocalDate releaseDate,
    Integer stock
) {
}
