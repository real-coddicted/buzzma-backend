package com.coddicted.buzzma.agency.api;

import jakarta.annotation.Nullable;
import java.time.Instant;
import java.util.UUID;
import lombok.Builder;
import lombok.Value;
import lombok.extern.jackson.Jacksonized;

@Value
@Builder
@Jacksonized
public class AgenciesResponseDto {

  UUID id;

  String name;

  String agencyCode;

  UUID ownerUserId;

  String status;

  @Nullable UUID createdBy;

  @Nullable UUID updatedBy;

  Boolean isDeleted;

  @Nullable Instant createdAt;

  @Nullable Instant updatedAt;
}
