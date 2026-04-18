package com.mobo.catalog.service;

import com.mobo.catalog.api.DealsRequestDto;
import com.mobo.catalog.api.DealsResponseDto;
import java.util.List;
import java.util.UUID;

public interface DealService {

  List<DealsResponseDto> list(int limit, int offset);

  DealsResponseDto getById(UUID id);

  DealsResponseDto create(DealsRequestDto request);

  DealsResponseDto update(UUID id, DealsRequestDto request);

  void delete(UUID id);
}
