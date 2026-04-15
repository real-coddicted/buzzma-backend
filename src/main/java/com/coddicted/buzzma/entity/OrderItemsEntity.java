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
@Table(name = "order_items")
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
@DynamicInsert
@DynamicUpdate
public class OrderItemsEntity implements Auditable {
  @Id
  @GeneratedValue
  @UuidGenerator
  @Column(name = "id")
  private UUID id;

  @Column(name = "order_id")
  private UUID orderId;

  @Column(name = "product_id")
  private String productId;

  @Column(name = "title")
  private String title;

  @Column(name = "image")
  private String image;

  @Column(name = "price_at_purchase_paise")
  private Integer priceAtPurchasePaise;

  @Column(name = "commission_paise")
  private Integer commissionPaise;

  @Column(name = "campaign_id")
  private UUID campaignId;

  @Column(name = "deal_type")
  private String dealType;

  @Column(name = "quantity")
  private Integer quantity;

  @Column(name = "platform")
  private String platform;

  @Column(name = "brand_name")
  private String brandName;

  @Column(name = "is_deleted")
  private Boolean isDeleted;

  @Column(name = "created_by")
  private String createdBy;

  @Column(name = "updated_by")
  private String updatedBy;

  @Column(name = "created_at")
  private Instant createdAt;

  @Column(name = "updated_at")
  private Instant updatedAt;
}
