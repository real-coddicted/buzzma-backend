package com.coddicted.buzzma.dto;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Size;
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
public class CampaignsRequestDto {
  @NotBlank
  @Size(max = 200)
  private String title;

  @NotNull private UUID brandUserId;

  @NotBlank
  @Size(max = 200)
  private String brandName;

  @NotBlank
  @Size(max = 80)
  private String platform;

  @NotBlank private String image;
  @NotBlank private String productUrl;
  @NotNull private Integer originalPricePaise;
  @NotNull private Integer pricePaise;
  @NotNull private Integer payoutPaise;
  private Integer returnWindowDays;
  private String dealType;
  @NotNull private Integer totalSlots;
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
  @NotNull private Instant updatedAt;
  private Boolean isDeleted;
  private Boolean openToAll;
}
