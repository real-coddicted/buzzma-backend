package com.coddicted.buzzma.wallet.api;

import jakarta.annotation.Nullable;
import java.time.Instant;
import java.util.UUID;
import lombok.Builder;
import lombok.Value;
import lombok.extern.jackson.Jacksonized;

@Value
@Builder
@Jacksonized
public class PayoutsRequestDto {

  UUID beneficiaryUserId;

  UUID walletId;

  Integer amountPaise;

  @Nullable String currency;

  @Nullable String status;

  @Nullable String provider;

  @Nullable String providerRef;

  @Nullable String failureCode;

  @Nullable String failureMessage;

  @Nullable Instant requestedAt;

  @Nullable Instant processedAt;
}
