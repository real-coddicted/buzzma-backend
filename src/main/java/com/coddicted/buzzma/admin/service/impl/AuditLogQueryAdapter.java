package com.coddicted.buzzma.admin.service.impl;

import com.coddicted.buzzma.admin.api.AuditLogQueryPort;
import com.coddicted.buzzma.admin.api.AuditLogsResponseDto;
import com.coddicted.buzzma.admin.mapper.AuditLogsMapper;
import com.coddicted.buzzma.admin.persistence.AuditLogsRepository;
import com.coddicted.buzzma.shared.common.OffsetBasedPageRequest;
import java.util.List;
import java.util.stream.Collectors;
import org.springframework.data.domain.Sort;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
public class AuditLogQueryAdapter implements AuditLogQueryPort {

  private final AuditLogsRepository auditLogsRepository;
  private final AuditLogsMapper auditLogsMapper;

  public AuditLogQueryAdapter(
      AuditLogsRepository auditLogsRepository, AuditLogsMapper auditLogsMapper) {
    this.auditLogsRepository = auditLogsRepository;
    this.auditLogsMapper = auditLogsMapper;
  }

  @Override
  @Transactional(readOnly = true)
  public List<AuditLogsResponseDto> listByEntity(
      String entityType, String entityId, int limit, int offset) {
    var pageable =
        new OffsetBasedPageRequest(limit, offset, Sort.by(Sort.Direction.DESC, "createdAt"));
    return auditLogsRepository
        .findAllByEntityTypeAndEntityId(entityType, entityId, pageable)
        .stream()
        .map(auditLogsMapper::toResponse)
        .collect(Collectors.toList());
  }
}
