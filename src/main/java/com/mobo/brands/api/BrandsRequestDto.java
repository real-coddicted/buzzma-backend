package com.mobo.brands.api;

import jakarta.annotation.Nullable;
import jakarta.validation.constraints.NotBlank;
import java.util.UUID;
import lombok.Builder;
import lombok.Value;
import lombok.extern.jackson.Jacksonized;

@Value
@Builder
@Jacksonized
public class BrandsRequestDto {

  @NotBlank String name;

  @NotBlank String brandCode;

  UUID ownerUserId;

  @Nullable String[] connectedAgencyCodes;

  @Nullable String status;
}
