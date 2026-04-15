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
public class PushSubscriptionsRequestDto {
  @NotNull private UUID userId;
  @NotBlank private String app;
  @NotBlank private String endpoint;
  private Integer expirationTime;
  @NotBlank private String keysP256dh;
  @NotBlank private String keysAuth;
  private String userAgent;
  private Instant createdAt;
  @NotNull private Instant updatedAt;
  private Boolean isDeleted;
  private String createdBy;
  private String updatedBy;
}
