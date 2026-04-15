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
public class ShopperProfilesRequestDto {
  @NotNull private UUID userId;
  private String defaultMediatorCode;
  private String createdBy;
  private String updatedBy;
  private Instant createdAt;
  @NotNull private Instant updatedAt;
  private Boolean isDeleted;
}
