package com.mobo.dto;

import jakarta.annotation.Nullable;
import java.time.Instant;
import java.util.UUID;
import lombok.Builder;
import lombok.Value;
import lombok.extern.jackson.Jacksonized;

@Value
@Builder
@Jacksonized
public class SuspensionsResponseDto {

  UUID id;

  UUID targetUserId;

  String action;

  @Nullable String reason;

  UUID adminUserId;

  @Nullable Instant createdAt;
}
