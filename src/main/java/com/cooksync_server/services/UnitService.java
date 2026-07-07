package com.cooksync_server.services;

import java.util.List;
import java.util.Optional;

import org.springframework.stereotype.Service;
import org.springframework.util.StringUtils;
import org.springframework.validation.annotation.Validated;

import com.cooksync_server.dtos.request.unit.UnitRequestDTO;
import com.cooksync_server.dtos.response.unit.UnitResponse;
import com.cooksync_server.entities.Unit;
import com.cooksync_server.exceptions.ResourceAllReadyExistsException;
import com.cooksync_server.exceptions.ResourceNotFoundException;
import com.cooksync_server.repositories.UnitRepository;

import jakarta.transaction.Transactional;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;

@Service
@Validated
@RequiredArgsConstructor
public class UnitService {

    private final UnitRepository unitRepository;

    public List<UnitResponse> getAllUnits() {
        return UnitResponse.fromEntities(unitRepository.findAll());
    }

    public UnitResponse getUnitById(String id) {
        Unit unit = unitRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("Unit", id));
        return UnitResponse.fromEntity(unit);
    }

    @Transactional
    public UnitResponse createUnit(@Valid UnitRequestDTO request) {
        if (request == null || request.code() == null || request.name() == null) {
            throw new IllegalArgumentException("Unit request, code, and name cannot be null");
        }

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

        return UnitResponse.fromEntity(unitRepository.save(newUnit));
    }

    @Transactional
    public void deleteUnit(String id) {
        Unit unit = unitRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("Unit", id));
        unitRepository.delete(unit);
    }
}