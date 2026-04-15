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
public class MediatorProfilesRequestDto {
  @NotNull private UUID userId;
  @NotBlank private String mediatorCode;
  private String parentAgencyCode;
  private String status;
  private String createdBy;
  private String updatedBy;
  private Instant createdAt;
  @NotNull private Instant updatedAt;
  private Boolean isDeleted;
}
