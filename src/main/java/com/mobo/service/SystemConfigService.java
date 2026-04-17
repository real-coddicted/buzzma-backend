package com.mobo.service;

import com.mobo.dto.SystemConfigsRequestDto;
import com.mobo.dto.SystemConfigsResponseDto;
import java.util.List;
import java.util.UUID;

public interface SystemConfigService {

  List<SystemConfigsResponseDto> list(int limit, int offset);

  SystemConfigsResponseDto getById(UUID id);

  SystemConfigsResponseDto create(SystemConfigsRequestDto request);

  SystemConfigsResponseDto update(UUID id, SystemConfigsRequestDto request);

  void delete(UUID id);
}
