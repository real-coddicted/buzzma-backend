package com.coddicted.buzzma.wallet.api;

import jakarta.annotation.Nullable;
import jakarta.validation.constraints.NotBlank;
import java.util.UUID;
import lombok.Builder;
import lombok.Value;
import lombok.extern.jackson.Jacksonized;

@Value
@Builder
@Jacksonized
public class TransactionsRequestDto {

  @NotBlank String idempotencyKey;

  @NotBlank String type;

  @Nullable String status;

  Integer amountPaise;

  @Nullable String currency;

  @Nullable String orderId;

  @Nullable UUID campaignId;

  @Nullable UUID payoutId;

  @Nullable UUID walletId;

  @Nullable UUID fromUserId;

  @Nullable UUID toUserId;

  @Nullable String metadata;
}
