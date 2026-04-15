package com.coddicted.buzzma.common;

import java.time.Instant;

public interface Auditable {
  Instant getCreatedAt();

  void setCreatedAt(Instant createdAt);

  Instant getUpdatedAt();

  void setUpdatedAt(Instant updatedAt);
}
