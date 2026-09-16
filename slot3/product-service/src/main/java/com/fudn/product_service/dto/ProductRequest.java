package com.fudn.product_service.dto;

import java.math.BigDecimal;
//dto request
public record ProductRequest(String name, String description, BigDecimal price) {
}
