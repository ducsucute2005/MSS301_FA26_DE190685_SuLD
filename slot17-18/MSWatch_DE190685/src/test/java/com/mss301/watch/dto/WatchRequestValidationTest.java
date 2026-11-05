package com.mss301.watch.dto;

import jakarta.validation.Validation;
import jakarta.validation.Validator;
import org.junit.jupiter.api.AfterAll;
import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.Test;

import java.math.BigDecimal;
import java.time.LocalDate;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertTrue;

class WatchRequestValidationTest {

    private static Validator validator;
    private static jakarta.validation.ValidatorFactory validatorFactory;

    @BeforeAll
    static void setUpValidator() {
        validatorFactory = Validation.buildDefaultValidatorFactory();
        validator = validatorFactory.getValidator();
    }

    @AfterAll
    static void closeValidatorFactory() {
        validatorFactory.close();
    }

    @Test
    void acceptsRequestMeetingFieldConstraints() {
        WatchRequest request = validRequest();

        assertTrue(validator.validate(request).isEmpty());
    }

    @Test
    void rejectsShortNameLowPriceAndNegativeStock() {
        WatchRequest request = validRequest();
        request.setName("Short");
        request.setPrice(new BigDecimal("99.99"));
        request.setStock(-1);

        var violations = validator.validate(request);

        assertEquals(3, violations.size());
    }

    @Test
    void rejectsMissingRequiredFields() {
        WatchRequest request = new WatchRequest();

        assertEquals(9, validator.validate(request).size());
    }

    private WatchRequest validRequest() {
        WatchRequest request = new WatchRequest();
        request.setName("A Fully Valid Watch");
        request.setCategoryId(1);
        request.setBrandId(1);
        request.setCaseMaterial("Steel");
        request.setStrapMaterial("Leather");
        request.setWaterpMaterial("50 meters");
        request.setPrice(new BigDecimal("100.00"));
        request.setReleaseDate(LocalDate.now());
        request.setStock(0);
        return request;
    }
}
