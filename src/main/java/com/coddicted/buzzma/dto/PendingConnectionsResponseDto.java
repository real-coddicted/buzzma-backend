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
public class PendingConnectionsResponseDto {
  private UUID id;
  private UUID userId;
  private String agencyId;
  private String agencyName;
  private String agencyCode;
  private Instant timestamp;
  private Boolean isDeleted;
  private String createdBy;
  private String updatedBy;
  private Instant createdAt;
  private Instant updatedAt;
}
