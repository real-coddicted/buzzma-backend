package com.mobo.dto;

import jakarta.annotation.Nullable;
import java.time.Instant;
import java.util.UUID;
import lombok.Builder;
import lombok.Value;
import lombok.extern.jackson.Jacksonized;

@Value
@Builder
@Jacksonized
public class AuditLogsResponseDto {

  UUID id;

  @Nullable UUID actorUserId;

  @Nullable String[] actorRoles;

  String action;

  @Nullable String entityType;

  @Nullable String entityId;

  @Nullable String ip;

  @Nullable String userAgent;

  @Nullable String metadata;

  @Nullable Instant createdAt;
}
