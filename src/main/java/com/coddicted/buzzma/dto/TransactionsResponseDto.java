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
public class TransactionsResponseDto {
  private UUID id;
  private String idempotencyKey;
  private String type;
  private String status;
  private Integer amountPaise;
  private String currency;
  private String orderId;
  private UUID campaignId;
  private UUID payoutId;
  private UUID walletId;
  private UUID fromUserId;
  private UUID toUserId;
  private String metadata;
  private String createdBy;
  private String updatedBy;
  private Instant createdAt;
  private Instant updatedAt;
  private Boolean isDeleted;
}
