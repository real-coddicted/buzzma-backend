package com.coddicted.buzzma.admin.service;

import com.coddicted.buzzma.admin.api.AuditLogsRequestDto;
import com.coddicted.buzzma.admin.api.AuditLogsResponseDto;
import java.util.List;
import java.util.UUID;

public interface AuditLogService {

  List<AuditLogsResponseDto> list(int limit, int offset);

  AuditLogsResponseDto getById(UUID id);

  AuditLogsResponseDto create(AuditLogsRequestDto request);

  AuditLogsResponseDto update(UUID id, AuditLogsRequestDto request);

  void delete(UUID id);
}
