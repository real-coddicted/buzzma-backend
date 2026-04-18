package com.coddicted.buzzma.orders.api;

import jakarta.annotation.Nullable;
import java.util.UUID;
import lombok.Builder;
import lombok.Value;
import lombok.extern.jackson.Jacksonized;

@Value
@Builder
@Jacksonized
public class OrderItemsResponseDto {

  UUID id;

  UUID orderId;

  String productId;

  String title;

  String image;

  Integer priceAtPurchasePaise;

  Integer commissionPaise;

  UUID campaignId;

  @Nullable String dealType;

  Integer quantity;

  @Nullable String platform;

  @Nullable String brandName;

  Boolean isDeleted;
}
