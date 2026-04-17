package com.mobo.common;

import com.mobo.entity.AuditLogsEntity;
import com.mobo.repository.AuditLogsRepository;
import java.time.Instant;
import java.util.UUID;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.scheduling.annotation.Async;
import org.springframework.stereotype.Component;

@Component
public class AuditLogWriter {

  private static final Logger LOG = LoggerFactory.getLogger(AuditLogWriter.class);

  private final AuditLogsRepository auditLogsRepository;

  public AuditLogWriter(AuditLogsRepository auditLogsRepository) {
    this.auditLogsRepository = auditLogsRepository;
  }

  @Async
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
