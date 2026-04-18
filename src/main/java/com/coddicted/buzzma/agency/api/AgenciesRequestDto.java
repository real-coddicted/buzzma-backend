package com.coddicted.buzzma.agency.api;

import jakarta.annotation.Nullable;
import jakarta.validation.constraints.NotBlank;
import java.util.UUID;
import lombok.Builder;
import lombok.Value;
import lombok.extern.jackson.Jacksonized;

@Value
@Builder
@Jacksonized
public class AgenciesRequestDto {

  @NotBlank String name;

  @NotBlank String agencyCode;

  UUID ownerUserId;

  @Nullable String status;
}
