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
public class PushSubscriptionsResponseDto {
  private UUID id;
  private UUID userId;
  private String app;
  private String endpoint;
  private Integer expirationTime;
  private String keysP256dh;
  private String keysAuth;
  private String userAgent;
  private Instant createdAt;
  private Instant updatedAt;
  private Boolean isDeleted;
  private String createdBy;
  private String updatedBy;
}
