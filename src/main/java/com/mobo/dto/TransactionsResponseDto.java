package com.mobo.dto;

import jakarta.annotation.Nullable;
import java.time.Instant;
import java.util.UUID;
import lombok.Builder;
import lombok.Value;
import lombok.extern.jackson.Jacksonized;

@Value
@Builder
@Jacksonized
public class TransactionsResponseDto {

  UUID id;

  String idempotencyKey;

  String type;

  String status;

  Integer amountPaise;

  String currency;

  @Nullable String orderId;

  @Nullable UUID campaignId;

  @Nullable UUID payoutId;

  @Nullable UUID walletId;

  @Nullable UUID fromUserId;

  @Nullable UUID toUserId;

  @Nullable String metadata;

  Boolean isDeleted;

  @Nullable Instant createdAt;

  @Nullable Instant updatedAt;
}
