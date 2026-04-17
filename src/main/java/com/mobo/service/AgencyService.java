package com.mobo.service;

import com.mobo.dto.AgenciesRequestDto;
import com.mobo.dto.AgenciesResponseDto;
import java.util.List;
import java.util.UUID;

public interface AgencyService {

  List<AgenciesResponseDto> list(int limit, int offset);

  AgenciesResponseDto getById(UUID id);

  AgenciesResponseDto create(AgenciesRequestDto request);

  AgenciesResponseDto update(UUID id, AgenciesRequestDto request);

  void delete(UUID id);
}
