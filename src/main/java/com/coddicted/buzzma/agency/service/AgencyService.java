package com.coddicted.buzzma.agency.service;

import com.coddicted.buzzma.agency.api.AgenciesRequestDto;
import com.coddicted.buzzma.agency.api.AgenciesResponseDto;
import java.util.List;
import java.util.UUID;

public interface AgencyService {

  List<AgenciesResponseDto> list(int limit, int offset);

  AgenciesResponseDto getById(UUID id);

  AgenciesResponseDto create(AgenciesRequestDto request);

  AgenciesResponseDto update(UUID id, AgenciesRequestDto request);

  void delete(UUID id);
}
