package com.mobo.admin.persistence;

import java.util.UUID;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;

public interface AuditLogsRepository extends JpaRepository<AuditLogsEntity, UUID> {

  Page<AuditLogsEntity> findAllByEntityTypeAndEntityId(
      String entityType, String entityId, Pageable pageable);

  Page<AuditLogsEntity> findAllByActorUserId(UUID actorUserId, Pageable pageable);
}
