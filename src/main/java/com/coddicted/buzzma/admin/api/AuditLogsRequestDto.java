package com.coddicted.buzzma.admin.api;

import jakarta.annotation.Nullable;
import jakarta.validation.constraints.NotBlank;
import java.util.UUID;
import lombok.Builder;
import lombok.Value;
import lombok.extern.jackson.Jacksonized;

@Value
@Builder
@Jacksonized
public class AuditLogsRequestDto {

  @Nullable UUID actorUserId;

  @Nullable String[] actorRoles;

  @NotBlank String action;

  @Nullable String entityType;

  @Nullable String entityId;

  @Nullable String ip;

  @Nullable String userAgent;

  @Nullable String metadata;
}
