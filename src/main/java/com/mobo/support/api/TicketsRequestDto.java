package com.mobo.support.api;

import jakarta.annotation.Nullable;
import jakarta.validation.constraints.NotBlank;
import java.util.UUID;
import lombok.Builder;
import lombok.Value;
import lombok.extern.jackson.Jacksonized;

@Value
@Builder
@Jacksonized
public class TicketsRequestDto {

  UUID userId;

  @NotBlank String userName;

  @NotBlank String role;

  @Nullable String orderId;

  @NotBlank String issueType;

  @NotBlank String description;

  @Nullable String status;

  @Nullable String targetRole;

  @Nullable String priority;

  @Nullable UUID resolvedBy;

  @Nullable String resolutionNote;
}
