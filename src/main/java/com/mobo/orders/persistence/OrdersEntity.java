package com.mobo.orders.persistence;

import com.mobo.shared.common.AuditEntityListener;
import com.mobo.shared.common.Auditable;
import com.mobo.shared.enums.AffiliateStatus;
import com.mobo.shared.enums.OrderStatus;
import com.mobo.shared.enums.OrderWorkflowStatus;
import com.mobo.shared.enums.PaymentStatus;
import com.mobo.shared.enums.RejectionType;
import com.mobo.shared.enums.SettlementMode;
import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.EntityListeners;
import jakarta.persistence.EnumType;
import jakarta.persistence.Enumerated;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.Id;
import jakarta.persistence.Table;
import java.time.Instant;
import java.util.UUID;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;
import org.hibernate.annotations.UuidGenerator;

@Entity
@Table(name = "orders")
@EntityListeners(AuditEntityListener.class)
@Getter
@Setter
@NoArgsConstructor
public class OrdersEntity implements Auditable {

  @Id
  @GeneratedValue
  @UuidGenerator
  @Column(name = "id", updatable = false, nullable = false)
  private UUID id;

  @Column(name = "user_id", nullable = false)
  private UUID userId;

  @Column(name = "brand_user_id")
  private UUID brandUserId;

  @Column(name = "total_paise", nullable = false)
  private Integer totalPaise;

  @Enumerated(EnumType.STRING)
  @Column(name = "workflow_status")
  private OrderWorkflowStatus workflowStatus = OrderWorkflowStatus.CREATED;

  @Column(name = "frozen")
  private Boolean frozen = false;

  @Column(name = "frozen_at")
  private Instant frozenAt;

  @Column(name = "frozen_reason")
  private String frozenReason;

  @Column(name = "reactivated_at")
  private Instant reactivatedAt;

  @Column(name = "reactivated_by")
  private UUID reactivatedBy;

  @Enumerated(EnumType.STRING)
  @Column(name = "status")
  private OrderStatus status = OrderStatus.Ordered;

  @Enumerated(EnumType.STRING)
  @Column(name = "payment_status")
  private PaymentStatus paymentStatus = PaymentStatus.Pending;

  @Enumerated(EnumType.STRING)
  @Column(name = "affiliate_status")
  private AffiliateStatus affiliateStatus = AffiliateStatus.Unchecked;

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

  @Enumerated(EnumType.STRING)
  @Column(name = "settlement_mode")
  private SettlementMode settlementMode = SettlementMode.wallet;

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
  private Integer returnWindowDays = 7;

  @Column(name = "order_ai_verification", columnDefinition = "jsonb")
  private String orderAiVerification;

  @Column(name = "rating_ai_verification", columnDefinition = "jsonb")
  private String ratingAiVerification;

  @Column(name = "return_window_ai_verification", columnDefinition = "jsonb")
  private String returnWindowAiVerification;

  @Enumerated(EnumType.STRING)
  @Column(name = "rejection_type")
  private RejectionType rejectionType;

  @Column(name = "rejection_reason")
  private String rejectionReason;

  @Column(name = "rejection_at")
  private Instant rejectionAt;

  @Column(name = "rejection_by")
  private UUID rejectionBy;

  @Column(name = "verification", columnDefinition = "jsonb")
  private String verification;

  @Column(name = "manager_name", nullable = false)
  private String managerName;

  @Column(name = "agency_name")
  private String agencyName;

  @Column(name = "buyer_name", nullable = false)
  private String buyerName;

  @Column(name = "buyer_mobile", length = 10, nullable = false)
  private String buyerMobile;

  @Column(name = "reviewer_name")
  private String reviewerName;

  @Column(name = "brand_name")
  private String brandName;

  @Column(name = "events", columnDefinition = "jsonb")
  private String events = "[]";

  @Column(name = "missing_proof_requests", columnDefinition = "jsonb")
  private String missingProofRequests = "[]";

  @Column(name = "expected_settlement_date")
  private Instant expectedSettlementDate;

  @Column(name = "created_by")
  private UUID createdBy;

  @Column(name = "updated_by")
  private UUID updatedBy;

  @Column(name = "is_deleted", nullable = false)
  private Boolean isDeleted = false;

  @Column(name = "created_at", nullable = false, updatable = false)
  private Instant createdAt;

  @Column(name = "updated_at", nullable = false)
  private Instant updatedAt;
}
