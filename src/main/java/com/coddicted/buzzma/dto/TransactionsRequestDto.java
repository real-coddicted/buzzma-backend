package com.coddicted.buzzma.dto;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Size;
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
public class TransactionsRequestDto {
  @NotBlank private String idempotencyKey;
  @NotBlank private String type;
  private String status;
  @NotNull private Integer amountPaise;
  private String currency;

  @Size(max = 64)
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
  @NotNull private Instant updatedAt;
  private Boolean isDeleted;
}
