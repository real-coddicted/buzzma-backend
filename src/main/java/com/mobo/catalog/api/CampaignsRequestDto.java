package com.mobo.catalog.api;

import jakarta.annotation.Nullable;
import jakarta.validation.constraints.NotBlank;
import java.util.UUID;
import lombok.Builder;
import lombok.Value;
import lombok.extern.jackson.Jacksonized;

@Value
@Builder
@Jacksonized
public class CampaignsRequestDto {

  @NotBlank String title;

  UUID brandUserId;

  @NotBlank String brandName;

  @NotBlank String platform;

  @NotBlank String image;

  @NotBlank String productUrl;

  Integer originalPricePaise;

  Integer pricePaise;

  Integer payoutPaise;

  @Nullable Integer returnWindowDays;

  @Nullable String dealType;

  Integer totalSlots;

  @Nullable String status;

  @Nullable String[] allowedAgencyCodes;

  @Nullable String assignments;

  @Nullable Boolean openToAll;
}
