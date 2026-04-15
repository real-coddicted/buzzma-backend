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
public class CampaignsResponseDto {
  private UUID id;
  private String title;
  private UUID brandUserId;
  private String brandName;
  private String platform;
  private String image;
  private String productUrl;
  private Integer originalPricePaise;
  private Integer pricePaise;
  private Integer payoutPaise;
  private Integer returnWindowDays;
  private String dealType;
  private Integer totalSlots;
  private Integer usedSlots;
  private String status;
  private String[] allowedAgencyCodes;
  private String assignments;
  private Boolean locked;
  private Instant lockedAt;
  private String lockedReason;
  private String createdBy;
  private String updatedBy;
  private Instant createdAt;
  private Instant updatedAt;
  private Boolean isDeleted;
  private Boolean openToAll;
}
