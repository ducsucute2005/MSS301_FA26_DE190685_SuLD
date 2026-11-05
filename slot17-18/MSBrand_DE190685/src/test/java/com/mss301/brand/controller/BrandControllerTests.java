package com.mss301.brand.controller;

import com.mss301.brand.dto.BrandResponse;
import com.mss301.brand.dto.BrandWatchesResponse;
import com.mss301.brand.exception.GlobalExceptionHandler;
import com.mss301.brand.service.BrandService;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.webmvc.test.autoconfigure.WebMvcTest;
import org.springframework.context.annotation.Import;
import org.springframework.test.context.bean.override.mockito.MockitoBean;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.web.server.ResponseStatusException;

import java.util.List;

import static org.mockito.Mockito.when;
import static org.springframework.http.HttpStatus.NOT_FOUND;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

@WebMvcTest(BrandController.class)
@Import(GlobalExceptionHandler.class)
class BrandControllerTests {

    @Autowired
    private MockMvc mockMvc;

    @MockitoBean
    private BrandService brandService;

    @Test
    void listBrandsIncludesWatchCount() throws Exception {
        when(brandService.findAll())
                .thenReturn(List.of(new BrandResponse(1, "Rolex", "Switzerland", 2)));

        mockMvc.perform(get("/api/brands"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.success").value(true))
                .andExpect(jsonPath("$.data[0].brandName").value("Rolex"))
                .andExpect(jsonPath("$.data[0].watchCount").value(2));
    }

    @Test
    void getBrandByIdReturnsBrandDetails() throws Exception {
        when(brandService.findById(1))
                .thenReturn(new BrandResponse(1, "Rolex", "Switzerland", 2));

        mockMvc.perform(get("/api/brands/1"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.data.brandId").value(1))
                .andExpect(jsonPath("$.data.countryOfOrigin").value("Switzerland"))
                .andExpect(jsonPath("$.data.watchCount").value(2));
    }

    @Test
    void getBrandWatchesReturnsIdsAndCount() throws Exception {
        when(brandService.watchesOf(1))
                .thenReturn(new BrandWatchesResponse(1, "Rolex", 2, List.of(1, 8)));

        mockMvc.perform(get("/api/brands/1/watches"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.data.brandId").value(1))
                .andExpect(jsonPath("$.data.brandName").value("Rolex"))
                .andExpect(jsonPath("$.data.watchCount").value(2))
                .andExpect(jsonPath("$.data.watchIds[0]").value(1))
                .andExpect(jsonPath("$.data.watchIds[1]").value(8));
    }

    @Test
    void unknownBrandReturnsNotFoundApiResponse() throws Exception {
        when(brandService.findById(999))
                .thenThrow(new ResponseStatusException(NOT_FOUND, "Brand not found with id: 999"));

        mockMvc.perform(get("/api/brands/999"))
                .andExpect(status().isNotFound())
                .andExpect(jsonPath("$.success").value(false))
                .andExpect(jsonPath("$.message").value("Brand not found with id: 999"));
    }
}
