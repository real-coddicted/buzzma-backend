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
public class BrandsResponseDto {

  UUID id;

  String name;

  String brandCode;

  UUID ownerUserId;

  @Nullable String[] connectedAgencyCodes;

  String status;

  Boolean isDeleted;

  @Nullable Instant createdAt;

  @Nullable Instant updatedAt;
}
