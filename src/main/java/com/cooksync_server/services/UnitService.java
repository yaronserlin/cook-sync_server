package com.cooksync_server.services;

import java.util.List;
import java.util.Optional;

import org.springframework.stereotype.Service;
import org.springframework.util.StringUtils;

import com.dtos.request.unit.UnitRequestDTO;
import com.dtos.response.unit.UnitResponse;
import com.cooksync_server.entities.Unit;
import com.cooksync_server.exceptions.ResourceAllReadyExistsException;
import com.cooksync_server.exceptions.ResourceNotFoundException;
import com.cooksync_server.mappers.UnitMapper;
import com.cooksync_server.repositories.UnitRepository;

import jakarta.transaction.Transactional;
import lombok.RequiredArgsConstructor;

/**
 * Service class managing measurement unit creation, retrieval, and deletion.
 *
 * @author Yaron Serlin
 * @version 1.0
 * @since 02/08/2026
 */
@Service
@RequiredArgsConstructor
public class UnitService implements IUnitService{

    private final UnitRepository unitRepository;

    /**
     * Retrieves all measurement units configured in the system.
     *
     * Complexity:
     * Time: O(U) where U is total unit count
     * Space: O(U)
     *
     * @return list of UnitResponse DTOs
     */
    public com.dtos.response.PagedResponse<UnitResponse> getAllUnits(int page, int size) {
        org.springframework.data.domain.Page<Unit> unitPage = unitRepository.findAll(
            org.springframework.data.domain.PageRequest.of(page, size));
            
        List<UnitResponse> content = unitPage.getContent().stream().map(UnitMapper::toResponse).toList();
        
        return new com.dtos.response.PagedResponse<>(
                content,
                unitPage.getNumber(),
                unitPage.getSize(),
                unitPage.getTotalElements(),
                unitPage.getTotalPages(),
                unitPage.isLast()
        );
    }

    /**
     * Retrieves a measurement unit by ID.
     *
     * Complexity:
     * Time: O(1)
     * Space: O(1)
     *
     * @param id target unit ID
     * @return UnitResponse DTO
     */
    public UnitResponse getUnitById(String id) {
        Unit unit = unitRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("Unit", id));
        return UnitMapper.toResponse(unit);
    }

    /**
     * Creates a new measurement unit definition ensuring code uniqueness.
     *
     * Complexity:
     * Time: O(1)
     * Space: O(1)
     *
     * @param request unit creation request DTO
     * @return UnitResponse DTO of created unit
     */
    @Transactional
    public UnitResponse createUnit(UnitRequestDTO request) {
        String formattedCode = request.code().toLowerCase().trim();
        String formattedName = StringUtils.capitalize(request.name().toLowerCase().trim());

        Optional<Unit> existingUnitOpt = unitRepository.findByCode(formattedCode);
        if (existingUnitOpt.isPresent()) {
            Unit existingUnit = existingUnitOpt.get();
            throw new ResourceAllReadyExistsException("Unit: '" + existingUnit.getCode() + "'", existingUnit.getId());
        }

        Unit newUnit = Unit.builder()
                .name(formattedName)
                .code(formattedCode)
                .build();

        return UnitMapper.toResponse(unitRepository.save(newUnit));
    }

    /**
     * Deletes a measurement unit by ID.
     *
     * Complexity:
     * Time: O(1)
     * Space: O(1)
     *
     * @param id target unit ID
     */
    @Transactional
    public void deleteUnit(String id) {
        Unit unit = unitRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("Unit", id));
        unitRepository.delete(unit);
    }
}
