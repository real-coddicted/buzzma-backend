package com.mobo.wallet.api;

import java.util.List;
import java.util.Optional;
import java.util.UUID;

public interface WalletQueryPort {

  Optional<WalletsResponseDto> findByOwnerUserId(UUID ownerUserId);

  List<TransactionsResponseDto> listLedgerByOwnerUserId(UUID ownerUserId, int limit, int offset);

  List<PayoutsResponseDto> listPayouts(int limit, int offset);
}
