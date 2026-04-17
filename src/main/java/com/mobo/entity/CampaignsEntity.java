package com.mobo.entity;

import com.mobo.common.AuditEntityListener;
import com.mobo.common.Auditable;
import com.mobo.entity.enums.CampaignStatus;
import com.mobo.entity.enums.DealType;
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
@Table(name = "campaigns")
@EntityListeners(AuditEntityListener.class)
@Getter
@Setter
@NoArgsConstructor
public class CampaignsEntity implements Auditable {

  @Id
  @GeneratedValue
  @UuidGenerator
  @Column(name = "id", updatable = false, nullable = false)
  private UUID id;

  @Column(name = "title", length = 200, nullable = false)
  private String title;

  @Column(name = "brand_user_id", nullable = false)
  private UUID brandUserId;

  @Column(name = "brand_name", length = 200, nullable = false)
  private String brandName;

  @Column(name = "platform", length = 80, nullable = false)
  private String platform;

  @Column(name = "image", nullable = false)
  private String image;

  @Column(name = "product_url", nullable = false)
  private String productUrl;

  @Column(name = "original_price_paise", nullable = false)
  private Integer originalPricePaise;

  @Column(name = "price_paise", nullable = false)
  private Integer pricePaise;

  @Column(name = "payout_paise", nullable = false)
  private Integer payoutPaise;

  @Column(name = "return_window_days")
  private Integer returnWindowDays = 7;

  @Enumerated(EnumType.STRING)
  @Column(name = "deal_type")
  private DealType dealType;

  @Column(name = "total_slots", nullable = false)
  private Integer totalSlots;

  @Column(name = "used_slots")
  private Integer usedSlots = 0;

  @Enumerated(EnumType.STRING)
  @Column(name = "status")
  private CampaignStatus status = CampaignStatus.draft;

  @Column(name = "allowed_agency_codes", columnDefinition = "text[]")
  private String[] allowedAgencyCodes = new String[0];

  @Column(name = "assignments", columnDefinition = "jsonb")
  private String assignments = "{}";

  @Column(name = "open_to_all")
  private Boolean openToAll = false;

  @Column(name = "locked")
  private Boolean locked = false;

  @Column(name = "locked_at")
  private Instant lockedAt;

  @Column(name = "locked_reason")
  private String lockedReason;

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
