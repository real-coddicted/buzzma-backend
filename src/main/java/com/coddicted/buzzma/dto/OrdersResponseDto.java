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
public class OrdersResponseDto {
  private UUID id;
  private UUID userId;
  private UUID brandUserId;
  private Integer totalPaise;
  private String workflowStatus;
  private Boolean frozen;
  private Instant frozenAt;
  private String frozenReason;
  private Instant reactivatedAt;
  private UUID reactivatedBy;
  private String status;
  private String paymentStatus;
  private String affiliateStatus;
  private String externalOrderId;
  private Instant orderDate;
  private String soldBy;
  private String extractedProductName;
  private String settlementRef;
  private String settlementMode;
  private String screenshotOrder;
  private String screenshotPayment;
  private String screenshotReview;
  private String screenshotRating;
  private String screenshotReturnWindow;
  private String reviewLink;
  private Integer returnWindowDays;
  private String orderAiVerification;
  private String ratingAiVerification;
  private String returnWindowAiVerification;
  private String rejectionType;
  private String rejectionReason;
  private Instant rejectionAt;
  private UUID rejectionBy;
  private String verification;
  private String managerName;
  private String agencyName;
  private String buyerName;
  private String buyerMobile;
  private String reviewerName;
  private String brandName;
  private String events;
  private String missingProofRequests;
  private Instant expectedSettlementDate;
  private String createdBy;
  private String updatedBy;
  private Instant createdAt;
  private Instant updatedAt;
  private Boolean isDeleted;
}
