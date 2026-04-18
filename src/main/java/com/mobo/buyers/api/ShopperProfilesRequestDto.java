package com.mobo.buyers.api;

import jakarta.annotation.Nullable;
import java.util.UUID;
import lombok.Builder;
import lombok.Value;
import lombok.extern.jackson.Jacksonized;

@Value
@Builder
@Jacksonized
public class ShopperProfilesRequestDto {

  UUID userId;

  @Nullable String defaultMediatorCode;
}
