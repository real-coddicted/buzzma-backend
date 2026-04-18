package com.coddicted.buzzma.admin.api;

import jakarta.annotation.Nullable;
import java.time.Instant;
import java.util.UUID;
import lombok.Builder;
import lombok.Value;
import lombok.extern.jackson.Jacksonized;

@Value
@Builder
@Jacksonized
public class SystemConfigsResponseDto {

  UUID id;

  String key;

  @Nullable String adminContactEmail;

  @Nullable Instant createdAt;

  @Nullable Instant updatedAt;
}
