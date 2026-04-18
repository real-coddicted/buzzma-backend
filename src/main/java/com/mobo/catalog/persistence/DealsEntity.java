package com.mobo.catalog.persistence;

import com.mobo.shared.common.AuditEntityListener;
import com.mobo.shared.common.Auditable;
import com.mobo.shared.enums.DealType;
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
@Table(name = "deals")
@EntityListeners(AuditEntityListener.class)
@Getter
@Setter
@NoArgsConstructor
public class DealsEntity implements Auditable {

  @Id
  @GeneratedValue
  @UuidGenerator
  @Column(name = "id", updatable = false, nullable = false)
  private UUID id;

  @Column(name = "campaign_id", nullable = false)
  private UUID campaignId;

  @Column(name = "mediator_code", nullable = false)
  private String mediatorCode;

  @Column(name = "title", nullable = false)
  private String title;

  @Column(name = "description")
  private String description = "Exclusive";

  @Column(name = "image", nullable = false)
  private String image;

  @Column(name = "product_url", nullable = false)
  private String productUrl;

  @Column(name = "platform", nullable = false)
  private String platform;

  @Column(name = "brand_name", nullable = false)
  private String brandName;

  @Enumerated(EnumType.STRING)
  @Column(name = "deal_type", nullable = false)
  private DealType dealType;

  @Column(name = "original_price_paise", nullable = false)
  private Integer originalPricePaise;

  @Column(name = "price_paise", nullable = false)
  private Integer pricePaise;

  @Column(name = "commission_paise", nullable = false)
  private Integer commissionPaise;

  @Column(name = "payout_paise", nullable = false)
  private Integer payoutPaise;

  @Column(name = "rating")
  private Double rating = 5.0;

  @Column(name = "category")
  private String category = "General";

  @Column(name = "active")
  private Boolean active = true;

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
