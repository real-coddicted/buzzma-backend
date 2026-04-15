package com.coddicted.buzzma.entity;

import com.coddicted.buzzma.common.AuditEntityListener;
import com.coddicted.buzzma.common.Auditable;
import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.EntityListeners;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.Id;
import jakarta.persistence.Table;
import java.time.Instant;
import java.util.UUID;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;
import org.hibernate.annotations.DynamicInsert;
import org.hibernate.annotations.DynamicUpdate;
import org.hibernate.annotations.UuidGenerator;

@Entity
@EntityListeners(AuditEntityListener.class)
@Table(name = "orders")
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
@DynamicInsert
@DynamicUpdate
public class OrdersEntity implements Auditable {
  @Id
  @GeneratedValue
  @UuidGenerator
  @Column(name = "id")
  private UUID id;

  @Column(name = "user_id")
  private UUID userId;

  @Column(name = "brand_user_id")
  private UUID brandUserId;

  @Column(name = "total_paise")
  private Integer totalPaise;

  @Column(name = "workflow_status")
  private String workflowStatus;

  @Column(name = "frozen")
  private Boolean frozen;

  @Column(name = "frozen_at")
  private Instant frozenAt;

  @Column(name = "frozen_reason")
  private String frozenReason;

  @Column(name = "reactivated_at")
  private Instant reactivatedAt;

  @Column(name = "reactivated_by")
  private UUID reactivatedBy;

  @Column(name = "status")
  private String status;

  @Column(name = "payment_status")
  private String paymentStatus;

  @Column(name = "affiliate_status")
  private String affiliateStatus;

  @Column(name = "external_order_id")
  private String externalOrderId;

  @Column(name = "order_date")
  private Instant orderDate;

  @Column(name = "sold_by")
  private String soldBy;

  @Column(name = "extracted_product_name")
  private String extractedProductName;

  @Column(name = "settlement_ref")
  private String settlementRef;

  @Column(name = "settlement_mode")
  private String settlementMode;

  @Column(name = "screenshot_order")
  private String screenshotOrder;

  @Column(name = "screenshot_payment")
  private String screenshotPayment;

  @Column(name = "screenshot_review")
  private String screenshotReview;

  @Column(name = "screenshot_rating")
  private String screenshotRating;

  @Column(name = "screenshot_return_window")
  private String screenshotReturnWindow;

  @Column(name = "review_link")
  private String reviewLink;

  @Column(name = "return_window_days")
  private Integer returnWindowDays;

  @Column(name = "order_ai_verification", columnDefinition = "jsonb")
  private String orderAiVerification;

  @Column(name = "rating_ai_verification", columnDefinition = "jsonb")
  private String ratingAiVerification;

  @Column(name = "return_window_ai_verification", columnDefinition = "jsonb")
  private String returnWindowAiVerification;

  @Column(name = "rejection_type")
  private String rejectionType;

  @Column(name = "rejection_reason")
  private String rejectionReason;

  @Column(name = "rejection_at")
  private Instant rejectionAt;

  @Column(name = "rejection_by")
  private UUID rejectionBy;

  @Column(name = "verification", columnDefinition = "jsonb")
  private String verification;

  @Column(name = "manager_name")
  private String managerName;

  @Column(name = "agency_name")
  private String agencyName;

  @Column(name = "buyer_name")
  private String buyerName;

  @Column(name = "buyer_mobile")
  private String buyerMobile;

  @Column(name = "reviewer_name")
  private String reviewerName;

  @Column(name = "brand_name")
  private String brandName;

  @Column(name = "events", columnDefinition = "jsonb")
  private String events;

  @Column(name = "missing_proof_requests", columnDefinition = "jsonb")
  private String missingProofRequests;

  @Column(name = "expected_settlement_date")
  private Instant expectedSettlementDate;

  @Column(name = "created_by")
  private String createdBy;

  @Column(name = "updated_by")
  private String updatedBy;

  @Column(name = "created_at")
  private Instant createdAt;

  @Column(name = "updated_at")
  private Instant updatedAt;

  @Column(name = "is_deleted")
  private Boolean isDeleted;
}
