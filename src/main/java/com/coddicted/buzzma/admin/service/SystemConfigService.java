package com.coddicted.buzzma.admin.service;

import com.coddicted.buzzma.admin.api.SystemConfigsRequestDto;
import com.coddicted.buzzma.admin.api.SystemConfigsResponseDto;
import java.util.List;
import java.util.UUID;

public interface SystemConfigService {

  List<SystemConfigsResponseDto> list(int limit, int offset);

  SystemConfigsResponseDto getById(UUID id);

  SystemConfigsResponseDto create(SystemConfigsRequestDto request);

  SystemConfigsResponseDto update(UUID id, SystemConfigsRequestDto request);

  void delete(UUID id);
}
