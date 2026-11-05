package com.mss301.brand.controller;

import com.mss301.brand.dto.AssignmentResponse;
import com.mss301.brand.exception.GlobalExceptionHandler;
import com.mss301.brand.service.BrandService;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.webmvc.test.autoconfigure.WebMvcTest;
import org.springframework.context.annotation.Import;
import org.springframework.test.context.bean.override.mockito.MockitoBean;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.web.server.ResponseStatusException;

import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;
import static org.springframework.http.HttpStatus.NOT_FOUND;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.delete;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.put;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

@WebMvcTest(InternalBrandController.class)
@Import(GlobalExceptionHandler.class)
class InternalBrandControllerTests {

    @Autowired
    private MockMvc mockMvc;

    @MockitoBean
    private BrandService brandService;

    @Test
    void putAssignReturnsAssignmentResponse() throws Exception {
        when(brandService.assignWatch(1, 99))
                .thenReturn(new AssignmentResponse(99, 1, null, "ASSIGNED"));

        mockMvc.perform(put("/internal/brands/1/watches/99"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.success").value(true))
                .andExpect(jsonPath("$.message").value("Assigned"))
                .andExpect(jsonPath("$.data.watchId").value(99))
                .andExpect(jsonPath("$.data.brandId").value(1))
                .andExpect(jsonPath("$.data.status").value("ASSIGNED"));

        verify(brandService).assignWatch(1, 99);
    }

    @Test
    void deleteUnassignReturnsNothingToDo() throws Exception {
        when(brandService.unassignWatch(99))
                .thenReturn(new AssignmentResponse(99, null, null, "NOTHING_TO_DO"));

        mockMvc.perform(delete("/internal/brands/watches/99"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.success").value(true))
                .andExpect(jsonPath("$.data.status").value("NOTHING_TO_DO"));

        verify(brandService).unassignWatch(99);
    }

    @Test
    void unknownBrandUsesApiErrorEnvelopeAndNotFoundStatus() throws Exception {
        when(brandService.assignWatch(999, 99))
                .thenThrow(new ResponseStatusException(NOT_FOUND, "Brand not found with id: 999"));

        mockMvc.perform(put("/internal/brands/999/watches/99"))
                .andExpect(status().isNotFound())
                .andExpect(jsonPath("$.success").value(false))
                .andExpect(jsonPath("$.message").value("Brand not found with id: 999"))
                .andExpect(jsonPath("$.data").doesNotExist());
    }
}
