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
public class TicketCommentsResponseDto {
  private UUID id;
  private UUID ticketId;
  private UUID userId;
  private String userName;
  private String role;
  private String message;
  private Boolean isDeleted;
  private Instant createdAt;
  private String createdBy;
  private String updatedBy;
  private Instant updatedAt;
}
