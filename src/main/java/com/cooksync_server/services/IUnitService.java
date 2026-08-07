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

public interface IUnitService {
    com.dtos.response.PagedResponse<UnitResponse> getAllUnits(int page, int size);
    UnitResponse getUnitById(String id);
    UnitResponse createUnit(UnitRequestDTO request);
    void deleteUnit(String id);
}