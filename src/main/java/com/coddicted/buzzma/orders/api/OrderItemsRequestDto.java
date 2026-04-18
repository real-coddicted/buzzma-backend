package com.coddicted.buzzma.orders.api;

import jakarta.annotation.Nullable;
import jakarta.validation.constraints.NotBlank;
import java.util.UUID;
import lombok.Builder;
import lombok.Value;
import lombok.extern.jackson.Jacksonized;

@Value
@Builder
@Jacksonized
public class OrderItemsRequestDto {

  UUID orderId;

  @NotBlank String productId;

  @NotBlank String title;

  @NotBlank String image;

  Integer priceAtPurchasePaise;

  Integer commissionPaise;

  UUID campaignId;

  @Nullable String dealType;

  @Nullable Integer quantity;

  @Nullable String platform;

  @Nullable String brandName;
}
