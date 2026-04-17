package com.mobo.dto;

import jakarta.annotation.Nullable;
import lombok.Builder;
import lombok.Value;
import lombok.extern.jackson.Jacksonized;

@Value
@Builder
@Jacksonized
public class SystemConfigsRequestDto {

  @Nullable String key;

  @Nullable String adminContactEmail;
}
