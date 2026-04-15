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
public class TicketsRequestDto {
  @NotNull private UUID userId;
  @NotBlank private String userName;
  @NotBlank private String role;
  private String orderId;
  @NotBlank private String issueType;
  @NotBlank private String description;
  private String status;
  private UUID resolvedBy;
  private Instant resolvedAt;

  @Size(max = 1000)
  private String resolutionNote;

  private String createdBy;
  private String updatedBy;
  private Instant createdAt;
  @NotNull private Instant updatedAt;
  private Boolean isDeleted;
  private String targetRole;
  private String priority;
}
