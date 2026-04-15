package com.coddicted.buzzma.dto;

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
public class WalletsRequestDto {
  @NotNull private UUID ownerUserId;
  private String currency;
  private Integer availablePaise;
  private Integer pendingPaise;
  private Integer lockedPaise;
  private Integer version;
  private String createdBy;
  private String updatedBy;
  private Instant createdAt;
  @NotNull private Instant updatedAt;
  private Boolean isDeleted;
}
