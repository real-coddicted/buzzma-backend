package com.mobo.wallet.api;

import java.util.UUID;

public interface WalletBusinessService {

  WalletsResponseDto ensureWallet(UUID ownerUserId);

  TransactionsResponseDto credit(WalletMutationRequest request);

  TransactionsResponseDto debit(WalletMutationRequest request);
}
