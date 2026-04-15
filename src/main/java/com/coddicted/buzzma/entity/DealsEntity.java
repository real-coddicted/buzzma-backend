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
@Table(name = "deals")
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
@DynamicInsert
@DynamicUpdate
public class DealsEntity implements Auditable {
  @Id
  @GeneratedValue
  @UuidGenerator
  @Column(name = "id")
  private UUID id;

  @Column(name = "campaign_id")
  private UUID campaignId;

  @Column(name = "mediator_code")
  private String mediatorCode;

  @Column(name = "title")
  private String title;

  @Column(name = "description")
  private String description;

  @Column(name = "image")
  private String image;

  @Column(name = "product_url")
  private String productUrl;

  @Column(name = "platform")
  private String platform;

  @Column(name = "brand_name")
  private String brandName;

  @Column(name = "deal_type")
  private String dealType;

  @Column(name = "original_price_paise")
  private Integer originalPricePaise;

  @Column(name = "price_paise")
  private Integer pricePaise;

  @Column(name = "commission_paise")
  private Integer commissionPaise;

  @Column(name = "payout_paise")
  private Integer payoutPaise;

  @Column(name = "rating")
  private Double rating;

  @Column(name = "category")
  private String category;

  @Column(name = "active")
  private Boolean active;

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
