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
public class OrdersRequestDto {
  @NotNull private UUID userId;
  private UUID brandUserId;
  @NotNull private Integer totalPaise;
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
  @NotBlank private String managerName;
  private String agencyName;
  @NotBlank private String buyerName;

  @NotBlank
  @Size(max = 10)
  private String buyerMobile;

  private String reviewerName;
  private String brandName;
  private String events;
  private String missingProofRequests;
  private Instant expectedSettlementDate;
  private String createdBy;
  private String updatedBy;
  private Instant createdAt;
  @NotNull private Instant updatedAt;
  private Boolean isDeleted;
}
