package com.cooksync_server.controllers;

import java.util.List;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import com.cooksync_server.dtos.request.unit.CreateUnitRequest;
import com.cooksync_server.dtos.response.ApiResponse;
import com.cooksync_server.dtos.response.unit.UnitResponse;
import com.cooksync_server.services.UnitService;

import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;

/**
 * REST controller responsible for handling administrative endpoints related to
 * measurement units. Restricts access to users possessing the 'ADMIN' role and
 * wraps outputs in a standardized API response.
 *
 * @author Yaron Serlin
 * @version Last Updated: 06/07/2026
 */
@RestController
@RequestMapping("/api/admin/units")
@PreAuthorize("hasRole('ADMIN')")
@RequiredArgsConstructor
public class UnitController {

    private final UnitService unitService;
    private static final Logger logger = LoggerFactory.getLogger(UnitController.class);

    /**
     * Retrieves a comprehensive list of all measurement units currently
     * available in the system.
     *
     * <p>
     * <b>Example:</b></p>
     * <pre>{@code
     * ResponseEntity<ApiResponse<List<UnitResponse>>> response = unitController.getAllUnits();
     * }</pre>
     *
     * @return A {@link ResponseEntity} containing an {@link ApiResponse}
     * wrapping a list of {@link UnitResponse} objects and an HTTP 200 OK
     * status.
     */
    @GetMapping("")
    public ResponseEntity<ApiResponse<List<UnitResponse>>> getAllUnits() {
        logger.info("Fetching all units from the system");
        List<UnitResponse> units = unitService.getAllUnits();
        return ResponseEntity.ok(new ApiResponse<>(true, units, null, "All units retrieved successfully"));
    }

    /**
     * Retrieves a specific measurement unit based on its unique identifier.
     *
     * <p>
     * <b>Example:</b></p>
     * <pre>{@code
     * ResponseEntity<ApiResponse<UnitResponse>> response = unitController.getUnitById("uuid-1234");
     * }</pre>
     *
     * @param id The unique string identifier of the measurement unit to
     * retrieve.
     * @return A {@link ResponseEntity} containing an {@link ApiResponse}
     * wrapping the requested {@link UnitResponse} and an HTTP 200 OK status.
     */
    @GetMapping("{id}")
    public ResponseEntity<ApiResponse<UnitResponse>> getUnitById(@PathVariable String id) {
        logger.info("Fetching unit with ID: {}", id);
        UnitResponse unit = unitService.getUnitById(id);
        return ResponseEntity.ok(new ApiResponse<>(true, unit, null, "Unit retrieved successfully"));
    }

    /**
     * Creates and stores a new measurement unit in the system based on the
     * provided request payload.
     *
     * <p>
     * <b>Example:</b></p>
     * <pre>{@code
     * CreateUnitRequest request = new CreateUnitRequest("Gram", "g");
     * ResponseEntity<ApiResponse<UnitResponse>> response = unitController.createUnit(request);
     * }</pre>
     *
     * @param unit The validated data transfer object containing the necessary
     * details for the new unit.
     * @return A {@link ResponseEntity} containing an {@link ApiResponse}
     * wrapping the newly created {@link UnitResponse} and an HTTP 200 OK
     * status.
     */
    @PostMapping("")
    public ResponseEntity<ApiResponse<UnitResponse>> createUnit(@Valid @RequestBody CreateUnitRequest unit) {
        logger.info("Creating new unit: {}", unit);
        UnitResponse createdUnit = unitService.createUnit(unit);
        return ResponseEntity.ok(new ApiResponse<>(true, createdUnit, null, "Unit created successfully"));
    }

    /**
     * Deletes a specific measurement unit from the system using its unique
     * identifier.
     *
     * <p>
     * <b>Example:</b></p>
     * <pre>{@code
     * ResponseEntity<ApiResponse<Void>> response = unitController.deleteUnit("uuid-1234");
     * }</pre>
     *
     * @param id The unique string identifier of the measurement unit targeted
     * for deletion.
     * @return A {@link ResponseEntity} containing an {@link ApiResponse} with
     * no content payload and an HTTP 200 OK status upon successful deletion.
     */
    @DeleteMapping("{id}")
    public ResponseEntity<ApiResponse<Void>> deleteUnit(@PathVariable String id) {
        logger.info("Deleting unit with ID: {}", id);
        unitService.deleteUnit(id);
        return ResponseEntity.ok(new ApiResponse<>(true, null, null, "Unit deleted successfully"));
    }
}
