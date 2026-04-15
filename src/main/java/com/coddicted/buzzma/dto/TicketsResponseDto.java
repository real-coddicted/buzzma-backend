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
public class TicketsResponseDto {
  private UUID id;
  private UUID userId;
  private String userName;
  private String role;
  private String orderId;
  private String issueType;
  private String description;
  private String status;
  private UUID resolvedBy;
  private Instant resolvedAt;
  private String resolutionNote;
  private String createdBy;
  private String updatedBy;
  private Instant createdAt;
  private Instant updatedAt;
  private Boolean isDeleted;
  private String targetRole;
  private String priority;
}
