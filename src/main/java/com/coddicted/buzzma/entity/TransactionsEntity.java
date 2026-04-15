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
@Table(name = "transactions")
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
@DynamicInsert
@DynamicUpdate
public class TransactionsEntity implements Auditable {
  @Id
  @GeneratedValue
  @UuidGenerator
  @Column(name = "id")
  private UUID id;

  @Column(name = "idempotency_key")
  private String idempotencyKey;

  @Column(name = "type")
  private String type;

  @Column(name = "status")
  private String status;

  @Column(name = "amount_paise")
  private Integer amountPaise;

  @Column(name = "currency")
  private String currency;

  @Column(name = "order_id")
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
