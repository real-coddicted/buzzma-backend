package com.mobo.service;

import com.mobo.dto.SuspensionsRequestDto;
import com.mobo.dto.SuspensionsResponseDto;
import java.util.List;
import java.util.UUID;

public interface SuspensionService {

  List<SuspensionsResponseDto> list(int limit, int offset);

  SuspensionsResponseDto getById(UUID id);

  SuspensionsResponseDto create(SuspensionsRequestDto request);

  SuspensionsResponseDto update(UUID id, SuspensionsRequestDto request);

  void delete(UUID id);
}
