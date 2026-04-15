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
@Table(name = "campaigns")
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
@DynamicInsert
@DynamicUpdate
public class CampaignsEntity implements Auditable {
  @Id
  @GeneratedValue
  @UuidGenerator
  @Column(name = "id")
  private UUID id;

  @Column(name = "title")
  private String title;

  @Column(name = "brand_user_id")
  private UUID brandUserId;

  @Column(name = "brand_name")
  private String brandName;

  @Column(name = "platform")
  private String platform;

  @Column(name = "image")
  private String image;

  @Column(name = "product_url")
  private String productUrl;

  @Column(name = "original_price_paise")
  private Integer originalPricePaise;

  @Column(name = "price_paise")
  private Integer pricePaise;

  @Column(name = "payout_paise")
  private Integer payoutPaise;

  @Column(name = "return_window_days")
  private Integer returnWindowDays;

  @Column(name = "deal_type")
  private String dealType;

  @Column(name = "total_slots")
  private Integer totalSlots;

  @Column(name = "used_slots")
  private Integer usedSlots;

  @Column(name = "status")
  private String status;

  @Column(name = "allowed_agency_codes")
  private String[] allowedAgencyCodes;

  @Column(name = "assignments", columnDefinition = "jsonb")
  private String assignments;

  @Column(name = "locked")
  private Boolean locked;

  @Column(name = "locked_at")
  private Instant lockedAt;

  @Column(name = "locked_reason")
  private String lockedReason;

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

  @Column(name = "open_to_all")
  private Boolean openToAll;
}
