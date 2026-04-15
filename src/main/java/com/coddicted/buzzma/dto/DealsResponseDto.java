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
public class DealsResponseDto {
  private UUID id;
  private UUID campaignId;
  private String mediatorCode;
  private String title;
  private String description;
  private String image;
  private String productUrl;
  private String platform;
  private String brandName;
  private String dealType;
  private Integer originalPricePaise;
  private Integer pricePaise;
  private Integer commissionPaise;
  private Integer payoutPaise;
  private Double rating;
  private String category;
  private Boolean active;
  private String createdBy;
  private String updatedBy;
  private Instant createdAt;
  private Instant updatedAt;
  private Boolean isDeleted;
}
