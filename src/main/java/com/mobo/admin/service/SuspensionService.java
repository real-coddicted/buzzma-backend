package com.mobo.admin.service;

import com.mobo.admin.api.SuspensionsRequestDto;
import com.mobo.admin.api.SuspensionsResponseDto;
import java.util.List;
import java.util.UUID;

public interface SuspensionService {

  List<SuspensionsResponseDto> list(int limit, int offset);

  SuspensionsResponseDto getById(UUID id);

  SuspensionsResponseDto create(SuspensionsRequestDto request);

  SuspensionsResponseDto update(UUID id, SuspensionsRequestDto request);

  void delete(UUID id);
}
