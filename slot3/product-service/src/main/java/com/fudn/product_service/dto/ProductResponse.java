package com.fudn.product_service.dto;

import java.math.BigDecimal;
//dto response
public record ProductResponse(String id, String name, String description, BigDecimal price) {
}
