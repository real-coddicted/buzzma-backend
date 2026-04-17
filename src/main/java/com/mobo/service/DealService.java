package com.mobo.service;

import com.mobo.dto.DealsRequestDto;
import com.mobo.dto.DealsResponseDto;
import java.util.List;
import java.util.UUID;

public interface DealService {

  List<DealsResponseDto> list(int limit, int offset);

  DealsResponseDto getById(UUID id);

  DealsResponseDto create(DealsRequestDto request);

  DealsResponseDto update(UUID id, DealsRequestDto request);

  void delete(UUID id);
}
