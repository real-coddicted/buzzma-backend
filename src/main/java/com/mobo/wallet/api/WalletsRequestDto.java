package com.mobo.wallet.api;

import jakarta.annotation.Nullable;
import java.util.UUID;
import lombok.Builder;
import lombok.Value;
import lombok.extern.jackson.Jacksonized;

@Value
@Builder
@Jacksonized
public class WalletsRequestDto {

  UUID ownerUserId;

  @Nullable String currency;

  @Nullable Integer availablePaise;

  @Nullable Integer pendingPaise;

  @Nullable Integer lockedPaise;

  @Nullable Integer version;
}
