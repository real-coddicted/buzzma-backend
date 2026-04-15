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
public class TicketCommentsRequestDto {
  @NotNull private UUID ticketId;
  @NotNull private UUID userId;
  @NotBlank private String userName;
  @NotBlank private String role;

  @NotBlank
  @Size(max = 2000)
  private String message;

  private Boolean isDeleted;
  private Instant createdAt;
  private String createdBy;
  private String updatedBy;
  private Instant updatedAt;
}
