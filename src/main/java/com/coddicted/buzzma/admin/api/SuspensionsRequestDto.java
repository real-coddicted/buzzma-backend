package com.coddicted.buzzma.admin.api;

import jakarta.annotation.Nullable;
import jakarta.validation.constraints.NotBlank;
import java.util.UUID;
import lombok.Builder;
import lombok.Value;
import lombok.extern.jackson.Jacksonized;

@Value
@Builder
@Jacksonized
public class SuspensionsRequestDto {

  UUID targetUserId;

  @NotBlank String action;

  @Nullable String reason;

  UUID adminUserId;
}
