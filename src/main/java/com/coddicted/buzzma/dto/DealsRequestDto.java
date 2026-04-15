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
public class DealsRequestDto {
  @NotNull private UUID campaignId;
  @NotBlank private String mediatorCode;
  @NotBlank private String title;
  private String description;
  @NotBlank private String image;
  @NotBlank private String productUrl;
  @NotBlank private String platform;
  @NotBlank private String brandName;
  @NotBlank private String dealType;
  @NotNull private Integer originalPricePaise;
  @NotNull private Integer pricePaise;
  @NotNull private Integer commissionPaise;
  @NotNull private Integer payoutPaise;
  private Double rating;
  private String category;
  private Boolean active;
  private String createdBy;
  private String updatedBy;
  private Instant createdAt;
  @NotNull private Instant updatedAt;
  private Boolean isDeleted;
}
