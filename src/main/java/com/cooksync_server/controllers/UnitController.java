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

import com.dtos.request.unit.UnitRequestDTO;
import com.dtos.response.ApiResponse;
import com.dtos.response.unit.UnitResponse;
import com.cooksync_server.services.IUnitService;

import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;

/**
 * REST Controller managing measurement unit definitions.
 * Publicly exposes unit retrieval endpoints, while creation and deletion require 'ADMIN' authority.
 *
 * @author Yaron Serlin
 * @version 1.0
 * @since 02/08/2026
 */
@RestController
@RequestMapping("/api/units")
@RequiredArgsConstructor
public class UnitController {

    private final IUnitService unitService;
    private static final Logger logger = LoggerFactory.getLogger(UnitController.class);

    /**
     * Retrieves all measurement units configured in the system.
     *
     * Complexity:
     * Time: O(U) where U is total unit count
     * Space: O(U)
     *
     * @return response entity containing list of UnitResponse DTOs
     */
    @GetMapping("")
    public ResponseEntity<ApiResponse<com.dtos.response.PagedResponse<UnitResponse>>> getAllUnits(
            @org.springframework.web.bind.annotation.RequestParam(defaultValue = "0") int page, 
            @org.springframework.web.bind.annotation.RequestParam(defaultValue = "20") int size) {
        logger.info("Fetching all units from the system");
        com.dtos.response.PagedResponse<UnitResponse> units = unitService.getAllUnits(page, size);
        return ResponseEntity.ok(new ApiResponse<>(true, units, null, "All units retrieved successfully"));
    }

    /**
     * Retrieves a specific measurement unit by ID.
     *
     * Complexity:
     * Time: O(1)
     * Space: O(1)
     *
     * @param id target unit unique identifier
     * @return response entity containing UnitResponse DTO
     */
    @PreAuthorize("hasRole('ADMIN')")
    @GetMapping("/{id}")
    public ResponseEntity<ApiResponse<UnitResponse>> getUnitById(@PathVariable String id) {
        logger.info("Fetching unit with ID: {}", id);
        UnitResponse unit = unitService.getUnitById(id);
        return ResponseEntity.ok(new ApiResponse<>(true, unit, null, "Unit retrieved successfully"));
    }

    /**
     * Creates a new measurement unit definition.
     *
     * Complexity:
     * Time: O(1)
     * Space: O(1)
     *
     * @param request unit creation request DTO
     * @return response entity containing created UnitResponse DTO
     */
    @PreAuthorize("hasRole('ADMIN')")
    @PostMapping("")
    public ResponseEntity<ApiResponse<UnitResponse>> createUnit(@Valid @RequestBody UnitRequestDTO request) {
        logger.info("Creating new unit: {}", request);
        UnitResponse createdUnit = unitService.createUnit(request);
        return ResponseEntity.status(HttpStatus.CREATED)
                .body(new ApiResponse<>(true, createdUnit, null, "Unit created successfully"));
    }

    /**
     * Deletes a measurement unit by ID.
     *
     * Complexity:
     * Time: O(1)
     * Space: O(1)
     *
     * @param id target unit unique identifier
     * @return response entity acknowledging unit deletion
     */
    @PreAuthorize("hasRole('ADMIN')")
    @DeleteMapping("/{id}")
    public ResponseEntity<ApiResponse<Void>> deleteUnit(@PathVariable String id) {
        logger.info("Deleting unit with ID: {}", id);
        unitService.deleteUnit(id);
        return ResponseEntity.ok(new ApiResponse<>(true, null, null, "Unit deleted successfully"));
    }
}
