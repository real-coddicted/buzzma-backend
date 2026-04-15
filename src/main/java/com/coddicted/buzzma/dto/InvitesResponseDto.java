package com.coddicted.buzzma.dto;

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
public class InvitesResponseDto {
  private UUID id;
  private String code;
  private String role;
  private String label;
  private UUID parentUserId;
  private String parentCode;
  private String status;
  private Integer maxUses;
  private Integer useCount;
  private Instant expiresAt;
  private String createdBy;
  private UUID usedBy;
  private Instant usedAt;
  private String uses;
  private UUID revokedBy;
  private Instant revokedAt;
  private Instant createdAt;
  private Instant updatedAt;
  private String updatedBy;
}
