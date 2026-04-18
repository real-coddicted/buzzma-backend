package com.mobo.admin.service.impl;

import com.mobo.admin.persistence.AuditLogsEntity;
import com.mobo.admin.persistence.AuditLogsRepository;
import com.mobo.shared.common.AuditLogWriter;
import java.time.Instant;
import java.util.UUID;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.scheduling.annotation.Async;
import org.springframework.stereotype.Component;

@Component
public class AuditLogWriterImpl implements AuditLogWriter {

  private static final Logger LOG = LoggerFactory.getLogger(AuditLogWriterImpl.class);

  private final AuditLogsRepository auditLogsRepository;

  public AuditLogWriterImpl(AuditLogsRepository auditLogsRepository) {
    this.auditLogsRepository = auditLogsRepository;
  }

  @Async
  @Override
  public void write(
      UUID actorUserId,
      String[] actorRoles,
      String action,
      String entityType,
      String entityId,
      String metadata) {
    try {
      AuditLogsEntity entry = new AuditLogsEntity();
      entry.setActorUserId(actorUserId);
      entry.setActorRoles(actorRoles != null ? actorRoles : new String[0]);
      entry.setAction(action);
      entry.setEntityType(entityType);
      entry.setEntityId(entityId);
      entry.setMetadata(metadata);
      entry.setCreatedAt(Instant.now());
      auditLogsRepository.save(entry);
    } catch (Exception e) {
      LOG.error("Failed to write audit log for action={}: {}", action, e.getMessage());
    }
  }
}
