package com.mobo.wallet.api;

import jakarta.annotation.Nullable;
import java.time.Instant;
import java.util.UUID;
import lombok.Builder;
import lombok.Value;
import lombok.extern.jackson.Jacksonized;

@Value
@Builder
@Jacksonized
public class PayoutsResponseDto {

  UUID id;

  UUID beneficiaryUserId;

  UUID walletId;

  Integer amountPaise;

  String currency;

  String status;

  @Nullable String provider;

  @Nullable String providerRef;

  @Nullable String failureCode;

  @Nullable String failureMessage;

  Instant requestedAt;

  @Nullable Instant processedAt;

  Boolean isDeleted;

  @Nullable Instant createdAt;

  @Nullable Instant updatedAt;
}
