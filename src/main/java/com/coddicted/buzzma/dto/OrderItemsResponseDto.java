package com.coddicted.buzzma.dto;

import java.time.Instant;
import java.util.UUID;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class OrderItemsResponseDto {
  private UUID id;
  private UUID orderId;
  private String productId;
  private String title;
  private String image;
  private Integer priceAtPurchasePaise;
  private Integer commissionPaise;
  private UUID campaignId;
  private String dealType;
  private Integer quantity;
  private String platform;
  private String brandName;
  private Boolean isDeleted;
  private String createdBy;
  private String updatedBy;
  private Instant createdAt;
  private Instant updatedAt;
}
