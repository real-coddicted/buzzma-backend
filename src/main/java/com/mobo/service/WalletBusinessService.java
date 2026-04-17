package com.mobo.service;

import com.mobo.dto.WalletMutationRequest;
import com.mobo.entity.TransactionsEntity;
import com.mobo.entity.WalletsEntity;
import java.util.UUID;

public interface WalletBusinessService {

  WalletsEntity ensureWallet(UUID ownerUserId);

  TransactionsEntity credit(WalletMutationRequest request);

  TransactionsEntity debit(WalletMutationRequest request);
}
