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
public class WalletsResponseDto {
  private UUID id;
  private UUID ownerUserId;
  private String currency;
  private Integer availablePaise;
  private Integer pendingPaise;
  private Integer lockedPaise;
  private Integer version;
  private String createdBy;
  private String updatedBy;
  private Instant createdAt;
  private Instant updatedAt;
  private Boolean isDeleted;
}
