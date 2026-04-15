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
public class SuspensionsRequestDto {
  @NotNull private UUID targetUserId;
  @NotBlank private String action;
  private String reason;
  @NotNull private UUID adminUserId;
  private Instant createdAt;
  private String createdBy;
  private String updatedBy;
  private Instant updatedAt;
}
