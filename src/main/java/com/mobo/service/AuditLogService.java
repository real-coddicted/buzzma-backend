package com.mobo.service;

import com.mobo.dto.AuditLogsRequestDto;
import com.mobo.dto.AuditLogsResponseDto;
import java.util.List;
import java.util.UUID;

public interface AuditLogService {

  List<AuditLogsResponseDto> list(int limit, int offset);

  AuditLogsResponseDto getById(UUID id);

  AuditLogsResponseDto create(AuditLogsRequestDto request);

  AuditLogsResponseDto update(UUID id, AuditLogsRequestDto request);

  void delete(UUID id);
}
