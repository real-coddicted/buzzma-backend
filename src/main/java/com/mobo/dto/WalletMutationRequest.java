package com.mobo.dto;

import com.mobo.entity.enums.TransactionType;
import java.util.UUID;
import lombok.Builder;
import lombok.Value;

@Value
@Builder
public class WalletMutationRequest {

  String idempotencyKey;

  TransactionType type;

  UUID ownerUserId;

  int amountPaise;

  UUID fromUserId;

  UUID toUserId;

  String orderId;

  UUID campaignId;

  UUID payoutId;

  String metadata;
}
