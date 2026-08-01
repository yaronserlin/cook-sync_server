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

@Service
@RequiredArgsConstructor
public class UnitService {

    private final UnitRepository unitRepository;

    public List<UnitResponse> getAllUnits() {
        return unitRepository.findAll().stream().map(UnitMapper::toResponse).toList();
    }

    public UnitResponse getUnitById(String id) {
        Unit unit = unitRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("Unit", id));
        return UnitMapper.toResponse(unit);
    }

    @Transactional
    public UnitResponse createUnit(UnitRequestDTO request) {
        // name/code non-blank is already enforced by @Valid on the controller's
        // @RequestBody (see UnitRequestDTO's @NotBlank constraints); no need to
        // re-check here.
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

    @Transactional
    public void deleteUnit(String id) {
        Unit unit = unitRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("Unit", id));
        unitRepository.delete(unit);
    }
}
