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
public class PayoutsResponseDto {
  private UUID id;
  private UUID beneficiaryUserId;
  private UUID walletId;
  private Integer amountPaise;
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
  private Instant updatedAt;
  private Boolean isDeleted;
}
