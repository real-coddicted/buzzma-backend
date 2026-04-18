package com.coddicted.buzzma.wallet.api;

import java.util.UUID;

public interface WalletAdminPort {

  PayoutsResponseDto createPayout(UUID beneficiaryUserId, int amountPaise, UUID actorUserId);

  void deletePayout(UUID payoutId, UUID actorUserId);
}
