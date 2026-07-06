package com.cooksync_server.services;

import java.util.List;
import java.util.Optional;

import org.springframework.stereotype.Service;
import org.springframework.util.StringUtils;
import org.springframework.validation.annotation.Validated;

import com.cooksync_server.dtos.request.unit.CreateUnitRequest;
import com.cooksync_server.dtos.response.unit.UnitResponse;
import com.cooksync_server.entities.Unit;
import com.cooksync_server.exceptions.ResourceAllReadyExistsException;
import com.cooksync_server.exceptions.ResourceNotFoundException;
import com.cooksync_server.repositories.UnitRepository;

import jakarta.transaction.Transactional;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;

/**
 * Service class responsible for managing measurement unit entities, including
 * retrieval, creation, and deletion operations, mapped directly to API response
 * objects.
 *
 * @author Yaron Serlin
 * @version Last Updated: 06/07/2026
 */
@Service
@Validated
@RequiredArgsConstructor
public class UnitService {

    private final UnitRepository unitRepository;

    /**
     * Retrieves all measurement units currently stored in the system and maps
     * them to response Data Transfer Objects.
     *
     * <p>
     * <b>Example:</b></p>
     * <pre>{@code
     * List<UnitResponse> units = unitService.getAllUnits();
     * }</pre>
     *
     * @return A {@link List} of {@link UnitResponse} objects representing all
     * available units.
     */
    public List<UnitResponse> getAllUnits() {
        return unitRepository.findAll().stream()
                .map(UnitResponse::fromEntity)
                .toList();
    }

    /**
     * Retrieves a specific measurement unit by its unique identifier and maps
     * it to a response Data Transfer Object.
     *
     * <p>
     * <b>Example:</b></p>
     * <pre>{@code
     * UnitResponse unit = unitService.getUnitById("uuid-1234");
     * }</pre>
     *
     * @param id The unique string identifier of the unit to retrieve.
     * @return The requested {@link UnitResponse} object.
     * @throws ResourceNotFoundException if no unit is found matching the
     * provided identifier.
     */
    public UnitResponse getUnitById(String id) {
        Unit unit = unitRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("Unit", id));
        return UnitResponse.fromEntity(unit);
    }

    /**
     * Creates and persists a new measurement unit in the system, ensuring no
     * duplicates exist by the unit code.
     *
     * <p>
     * <b>Example:</b></p>
     * <pre>{@code
     * CreateUnitRequest request = new CreateUnitRequest("Gram", "g");
     * UnitResponse savedUnit = unitService.createUnit(request);
     * }</pre>
     *
     * @param request The validated data transfer object containing the new
     * unit's name and code.
     * @return The persisted {@link UnitResponse} object containing its newly
     * generated database identifier.
     * @throws ResourceAllReadyExistsException if a unit with the exact same
     * code (case-insensitive) already exists.
     * @throws IllegalArgumentException if the request payload, unit code, or
     * unit name is null.
     */
    @Transactional
    public UnitResponse createUnit(@Valid CreateUnitRequest request) {
        if (request == null || request.getCode() == null || request.getName() == null) {
            throw new IllegalArgumentException("Unit request, code, and name cannot be null");
        }

        String formattedCode = request.getCode().toLowerCase().trim();
        String formattedName = StringUtils.capitalize(request.getName().toLowerCase().trim());

        Optional<Unit> existingUnitOpt = unitRepository.findByCode(formattedCode);
        if (existingUnitOpt.isPresent()) {
            Unit existingUnit = existingUnitOpt.get();
            throw new ResourceAllReadyExistsException("Unit: '" + existingUnit.getCode() + "'", existingUnit.getId());
        }

        Unit newUnit = Unit.builder()
                .name(formattedName)
                .code(formattedCode)
                .build();

        return UnitResponse.fromEntity(unitRepository.save(newUnit));
    }

    /**
     * Deletes a specific measurement unit from the system by its unique
     * database identifier.
     *
     * <p>
     * <b>Example:</b></p>
     * <pre>{@code
     * unitService.deleteUnit("uuid-1234");
     * }</pre>
     *
     * @param id The unique string identifier of the unit targeted for deletion.
     * @throws ResourceNotFoundException if the unit requested for deletion does
     * not exist in the database.
     */
    @Transactional
    public void deleteUnit(String id) {
        Unit unit = unitRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("Unit", id));
        unitRepository.delete(unit);
    }
}
