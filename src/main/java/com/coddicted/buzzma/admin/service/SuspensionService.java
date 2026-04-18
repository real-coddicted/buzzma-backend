package com.coddicted.buzzma.admin.service;

import com.coddicted.buzzma.admin.api.SuspensionsRequestDto;
import com.coddicted.buzzma.admin.api.SuspensionsResponseDto;
import java.util.List;
import java.util.UUID;

public interface SuspensionService {

  List<SuspensionsResponseDto> list(int limit, int offset);

  SuspensionsResponseDto getById(UUID id);

  SuspensionsResponseDto create(SuspensionsRequestDto request);

  SuspensionsResponseDto update(UUID id, SuspensionsRequestDto request);

  void delete(UUID id);
}
