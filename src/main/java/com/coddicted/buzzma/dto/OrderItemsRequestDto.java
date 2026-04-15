package com.coddicted.buzzma.dto;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
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
public class OrderItemsRequestDto {
  @NotNull private UUID orderId;
  @NotBlank private String productId;
  @NotBlank private String title;
  @NotBlank private String image;
  @NotNull private Integer priceAtPurchasePaise;
  @NotNull private Integer commissionPaise;
  @NotNull private UUID campaignId;
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
