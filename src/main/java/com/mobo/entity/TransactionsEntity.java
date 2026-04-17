package com.mobo.entity;

import com.mobo.common.AuditEntityListener;
import com.mobo.common.Auditable;
import com.mobo.entity.enums.TransactionStatus;
import com.mobo.entity.enums.TransactionType;
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
@Table(name = "transactions")
@EntityListeners(AuditEntityListener.class)
@Getter
@Setter
@NoArgsConstructor
public class TransactionsEntity implements Auditable {

  @Id
  @GeneratedValue
  @UuidGenerator
  @Column(name = "id", updatable = false, nullable = false)
  private UUID id;

  @Column(name = "idempotency_key", unique = true, nullable = false)
  private String idempotencyKey;

  @Enumerated(EnumType.STRING)
  @Column(name = "type", nullable = false)
  private TransactionType type;

  @Enumerated(EnumType.STRING)
  @Column(name = "status")
  private TransactionStatus status = TransactionStatus.pending;

  @Column(name = "amount_paise", nullable = false)
  private Integer amountPaise;

  @Column(name = "currency")
  private String currency = "INR";

  @Column(name = "order_id", length = 64)
  private String orderId;

  @Column(name = "campaign_id")
  private UUID campaignId;

  @Column(name = "payout_id")
  private UUID payoutId;

  @Column(name = "wallet_id")
  private UUID walletId;

  @Column(name = "from_user_id")
  private UUID fromUserId;

  @Column(name = "to_user_id")
  private UUID toUserId;

  @Column(name = "metadata", columnDefinition = "jsonb")
  private String metadata;

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
