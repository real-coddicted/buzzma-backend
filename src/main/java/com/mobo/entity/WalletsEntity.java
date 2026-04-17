package com.mobo.entity;

import com.mobo.common.AuditEntityListener;
import com.mobo.common.Auditable;
import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.EntityListeners;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.Id;
import jakarta.persistence.Table;
import jakarta.persistence.Version;
import java.time.Instant;
import java.util.UUID;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;
import org.hibernate.annotations.UuidGenerator;

@Entity
@Table(name = "wallets")
@EntityListeners(AuditEntityListener.class)
@Getter
@Setter
@NoArgsConstructor
public class WalletsEntity implements Auditable {

  @Id
  @GeneratedValue
  @UuidGenerator
  @Column(name = "id", updatable = false, nullable = false)
  private UUID id;

  @Column(name = "owner_user_id", unique = true, nullable = false)
  private UUID ownerUserId;

  @Column(name = "currency")
  private String currency = "INR";

  @Column(name = "available_paise")
  private Integer availablePaise = 0;

  @Column(name = "pending_paise")
  private Integer pendingPaise = 0;

  @Column(name = "locked_paise")
  private Integer lockedPaise = 0;

  @Version
  @Column(name = "version")
  private Integer version = 0;

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
