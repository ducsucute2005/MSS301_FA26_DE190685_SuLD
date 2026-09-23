package com.fudn.product_service.exception;


public class ProductNotFoundException extends RuntimeException {
    public ProductNotFoundException(String id) {
        super("not found product with id: " + id);
    }
}
