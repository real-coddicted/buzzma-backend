package com.coddicted.buzzma.catalog.api;

import jakarta.annotation.Nullable;
import java.time.Instant;
import java.util.UUID;
import lombok.Builder;
import lombok.Value;
import lombok.extern.jackson.Jacksonized;

@Value
@Builder
@Jacksonized
public class CampaignsResponseDto {

  UUID id;

  String title;

  UUID brandUserId;

  String brandName;

  String platform;

  String image;

  String productUrl;

  Integer originalPricePaise;

  Integer pricePaise;

  Integer payoutPaise;

  Integer returnWindowDays;

  @Nullable String dealType;

  Integer totalSlots;

  Integer usedSlots;

  String status;

  @Nullable String[] allowedAgencyCodes;

  @Nullable String assignments;

  Boolean openToAll;

  Boolean locked;

  @Nullable Instant lockedAt;

  @Nullable String lockedReason;

  Boolean isDeleted;

  @Nullable Instant createdAt;

  @Nullable Instant updatedAt;
}
