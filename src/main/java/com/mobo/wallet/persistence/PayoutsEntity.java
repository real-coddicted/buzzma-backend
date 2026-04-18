package com.mobo.wallet.persistence;

import com.mobo.shared.common.AuditEntityListener;
import com.mobo.shared.common.Auditable;
import com.mobo.shared.enums.PayoutStatus;
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
@Table(name = "payouts")
@EntityListeners(AuditEntityListener.class)
@Getter
@Setter
@NoArgsConstructor
public class PayoutsEntity implements Auditable {

  @Id
  @GeneratedValue
  @UuidGenerator
  @Column(name = "id", updatable = false, nullable = false)
  private UUID id;

  @Column(name = "beneficiary_user_id", nullable = false)
  private UUID beneficiaryUserId;

  @Column(name = "wallet_id", nullable = false)
  private UUID walletId;

  @Column(name = "amount_paise", nullable = false)
  private Integer amountPaise;

  @Column(name = "currency")
  private String currency = "INR";

  @Enumerated(EnumType.STRING)
  @Column(name = "status")
  private PayoutStatus status = PayoutStatus.requested;

  @Column(name = "provider")
  private String provider;

  @Column(name = "provider_ref")
  private String providerRef;

  @Column(name = "failure_code")
  private String failureCode;

  @Column(name = "failure_message")
  private String failureMessage;

  @Column(name = "requested_at", nullable = false)
  private Instant requestedAt = Instant.now();

  @Column(name = "processed_at")
  private Instant processedAt;

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
