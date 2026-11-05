package com.mss301.brand.controller;

import com.mss301.brand.dto.ApiResponse;
import com.mss301.brand.dto.AssignmentResponse;
import com.mss301.brand.service.BrandService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/internal/brands")
@RequiredArgsConstructor
@Tag(name = "Internal (Saga)", description = "Called only by MSWatch saga orchestrator")
public class InternalBrandController {

    private final BrandService brandService;

    @PutMapping("/{brandId}/watches/{watchId}")
    @Operation(summary = "Assign a watch to a brand (upsert, idempotent)")
    public ResponseEntity<ApiResponse<AssignmentResponse>> assign(
            @PathVariable Integer brandId,
            @PathVariable Integer watchId) {
        return ResponseEntity.ok(
                ApiResponse.success(brandService.assignWatch(brandId, watchId), "Assigned"));
    }

    @DeleteMapping("/watches/{watchId}")
    @Operation(summary = "Unassign a watch from its brand (idempotent)")
    public ResponseEntity<ApiResponse<AssignmentResponse>> unassign(@PathVariable Integer watchId) {
        return ResponseEntity.ok(
                ApiResponse.success(brandService.unassignWatch(watchId), "Unassigned"));
    }
}
