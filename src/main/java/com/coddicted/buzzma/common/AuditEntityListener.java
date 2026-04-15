package com.coddicted.buzzma.common;

import jakarta.persistence.PrePersist;
import jakarta.persistence.PreUpdate;
import java.time.Instant;

public class AuditEntityListener {

  @PrePersist
  public void prePersist(final Auditable entity) {
    final Instant now = Instant.now();
    if (entity.getCreatedAt() == null) {
      entity.setCreatedAt(now);
    }
    if (entity.getUpdatedAt() == null) {
      entity.setUpdatedAt(now);
    }
  }

  @PreUpdate
  public void preUpdate(final Auditable entity) {
    entity.setUpdatedAt(Instant.now());
  }
}
