package com.mobo.orders.api;

import jakarta.annotation.Nullable;
import java.time.Instant;
import java.util.UUID;
import lombok.Builder;
import lombok.Value;
import lombok.extern.jackson.Jacksonized;

@Value
@Builder
@Jacksonized
public class OrdersResponseDto {

  UUID id;

  UUID userId;

  @Nullable UUID brandUserId;

  Integer totalPaise;

  String workflowStatus;

  Boolean frozen;

  @Nullable Instant frozenAt;

  @Nullable String frozenReason;

  @Nullable Instant reactivatedAt;

  @Nullable UUID reactivatedBy;

  String status;

  String paymentStatus;

  String affiliateStatus;

  @Nullable String externalOrderId;

  String settlementMode;

  @Nullable String screenshotOrder;

  @Nullable String screenshotPayment;

  @Nullable String screenshotReview;

  @Nullable String screenshotRating;

  @Nullable String screenshotReturnWindow;

  @Nullable String reviewLink;

  Integer returnWindowDays;

  @Nullable String orderAiVerification;

  @Nullable String ratingAiVerification;

  @Nullable String returnWindowAiVerification;

  @Nullable String rejectionType;

  @Nullable String rejectionReason;

  @Nullable Instant rejectionAt;

  @Nullable UUID rejectionBy;

  @Nullable String verification;

  String managerName;

  @Nullable String agencyName;

  String buyerName;

  String buyerMobile;

  @Nullable String reviewerName;

  @Nullable String brandName;

  String events;

  String missingProofRequests;

  @Nullable Instant expectedSettlementDate;

  Boolean isDeleted;

  @Nullable Instant createdAt;

  @Nullable Instant updatedAt;
}
