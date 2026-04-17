package com.mobo.entity;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.Id;
import jakarta.persistence.Table;
import java.util.UUID;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;
import org.hibernate.annotations.UuidGenerator;

@Entity
@Table(name = "order_items")
@Getter
@Setter
@NoArgsConstructor
public class OrderItemsEntity {

  @Id
  @GeneratedValue
  @UuidGenerator
  @Column(name = "id", updatable = false, nullable = false)
  private UUID id;

  @Column(name = "order_id", nullable = false)
  private UUID orderId;

  @Column(name = "product_id", nullable = false)
  private String productId;

  @Column(name = "title", nullable = false)
  private String title;

  @Column(name = "image", nullable = false)
  private String image;

  @Column(name = "price_at_purchase_paise", nullable = false)
  private Integer priceAtPurchasePaise;

  @Column(name = "commission_paise", nullable = false)
  private Integer commissionPaise;

  @Column(name = "campaign_id", nullable = false)
  private UUID campaignId;

  @Column(name = "deal_type")
  private String dealType;

  @Column(name = "quantity")
  private Integer quantity = 1;

  @Column(name = "platform")
  private String platform;

  @Column(name = "brand_name")
  private String brandName;

  @Column(name = "is_deleted", nullable = false)
  private Boolean isDeleted = false;
}
