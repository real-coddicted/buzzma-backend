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
public class OrdersRequestDto {

  UUID userId;

  @Nullable UUID brandUserId;

  Integer totalPaise;

  @Nullable String workflowStatus;

  @Nullable String status;

  @Nullable String paymentStatus;

  @Nullable String affiliateStatus;

  @Nullable String externalOrderId;

  @Nullable String settlementMode;

  @Nullable String managerName;

  @Nullable String agencyName;

  String buyerName;

  String buyerMobile;

  @Nullable String reviewerName;

  @Nullable String brandName;

  @Nullable String events;

  @Nullable String missingProofRequests;

  @Nullable Instant expectedSettlementDate;

  @Nullable UUID reactivatedBy;

  @Nullable Integer returnWindowDays;
}
