package com.mss301.brand.controller;

import com.mss301.brand.dto.ApiResponse;
import com.mss301.brand.dto.BrandResponse;
import com.mss301.brand.dto.BrandWatchesResponse;
import com.mss301.brand.service.BrandService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.util.List;

@RestController
@RequestMapping("/api/brands")
@RequiredArgsConstructor
@Tag(name = "Brand", description = "Watch Brand management")
public class BrandController {

    private final BrandService brandService;

    @GetMapping
    @Operation(summary = "Get all brands with watch counts")
    public ResponseEntity<ApiResponse<List<BrandResponse>>> findAll() {
        return ResponseEntity.ok(ApiResponse.success(brandService.findAll(), "Success"));
    }

    @GetMapping("/{id}")
    @Operation(summary = "Get brand by ID")
    public ResponseEntity<ApiResponse<BrandResponse>> findById(@PathVariable Integer id) {
        return ResponseEntity.ok(ApiResponse.success(brandService.findById(id), "Success"));
    }

    @GetMapping("/{id}/watches")
    @Operation(summary = "Get watch IDs assigned to a brand")
    public ResponseEntity<ApiResponse<BrandWatchesResponse>> watches(@PathVariable Integer id) {
        return ResponseEntity.ok(ApiResponse.success(brandService.watchesOf(id), "Success"));
    }
}
