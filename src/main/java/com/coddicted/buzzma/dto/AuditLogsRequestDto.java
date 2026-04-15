package com.coddicted.buzzma.dto;

import jakarta.validation.constraints.NotBlank;
import java.time.Instant;
import java.util.UUID;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class AuditLogsRequestDto {
  private UUID actorUserId;
  private String[] actorRoles;
  @NotBlank private String action;
  private String entityType;
  private String entityId;
  private String ip;
  private String userAgent;
  private String metadata;
  private Instant createdAt;
  private String createdBy;
  private String updatedBy;
  private Instant updatedAt;
}
