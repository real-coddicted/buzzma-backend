package com.coddicted.buzzma.dto;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
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
public class InvitesRequestDto {
  @NotBlank private String code;
  @NotBlank private String role;
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
  @NotNull private Instant updatedAt;
  private String updatedBy;
}
