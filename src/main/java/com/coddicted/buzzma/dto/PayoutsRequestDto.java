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
public class PayoutsRequestDto {
  @NotNull private UUID beneficiaryUserId;
  @NotNull private UUID walletId;
  @NotNull private Integer amountPaise;
  private String currency;
  private String status;
  private String provider;
  private String providerRef;
  private String failureCode;
  private String failureMessage;
  private Instant requestedAt;
  private Instant processedAt;
  private String createdBy;
  private String updatedBy;
  private Instant createdAt;
  @NotNull private Instant updatedAt;
  private Boolean isDeleted;
}
