package com.cooksync_server.controllers;

import java.util.List;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import com.cooksync_server.dtos.request.unit.UnitRequestDTO;
import com.cooksync_server.dtos.response.ApiResponse;
import com.cooksync_server.dtos.response.unit.UnitResponse;
import com.cooksync_server.services.UnitService;

import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;

/**
 * REST controller responsible for handling administrative endpoints related to measurement units. 
 * Restricts access to users possessing the 'ADMIN' role.
 */
@RestController
@RequestMapping("/api/admin/units")
@PreAuthorize("hasRole('ADMIN')")
@RequiredArgsConstructor
public class UnitController {

    private final UnitService unitService;
    private static final Logger logger = LoggerFactory.getLogger(UnitController.class);

    @GetMapping("")
    public ResponseEntity<ApiResponse<List<UnitResponse>>> getAllUnits() {
        logger.info("Fetching all units from the system");
        List<UnitResponse> units = unitService.getAllUnits();
        return ResponseEntity.ok(new ApiResponse<>(true, units, null, "All units retrieved successfully"));
    }

    @GetMapping("/{id}")
    public ResponseEntity<ApiResponse<UnitResponse>> getUnitById(@PathVariable String id) {
        logger.info("Fetching unit with ID: {}", id);
        UnitResponse unit = unitService.getUnitById(id);
        return ResponseEntity.ok(new ApiResponse<>(true, unit, null, "Unit retrieved successfully"));
    }

    @PostMapping("")
    public ResponseEntity<ApiResponse<UnitResponse>> createUnit(@Valid @RequestBody UnitRequestDTO request) {
        logger.info("Creating new unit: {}", request);
        UnitResponse createdUnit = unitService.createUnit(request);
        return ResponseEntity.status(HttpStatus.CREATED)
                .body(new ApiResponse<>(true, createdUnit, null, "Unit created successfully"));
    }

    @DeleteMapping("/{id}")
    public ResponseEntity<ApiResponse<Void>> deleteUnit(@PathVariable String id) {
        logger.info("Deleting unit with ID: {}", id);
        unitService.deleteUnit(id);
        return ResponseEntity.ok(new ApiResponse<>(true, null, null, "Unit deleted successfully"));
    }
}