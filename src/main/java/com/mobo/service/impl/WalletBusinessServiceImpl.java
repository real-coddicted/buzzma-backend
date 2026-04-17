package com.mobo.service.impl;

import com.mobo.dto.WalletMutationRequest;
import com.mobo.entity.TransactionsEntity;
import com.mobo.entity.WalletsEntity;
import com.mobo.entity.enums.TransactionStatus;
import com.mobo.exception.ApiException;
import com.mobo.repository.TransactionsRepository;
import com.mobo.repository.WalletsRepository;
import com.mobo.service.WalletBusinessService;
import java.util.Optional;
import java.util.UUID;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.dao.DataIntegrityViolationException;
import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
public class WalletBusinessServiceImpl implements WalletBusinessService {

  @Value("${app.wallet.max-balance-paise:100000000}")
  private long maxBalancePaise;

  private final WalletsRepository walletsRepository;
  private final TransactionsRepository transactionsRepository;

  public WalletBusinessServiceImpl(
      WalletsRepository walletsRepository, TransactionsRepository transactionsRepository) {
    this.walletsRepository = walletsRepository;
    this.transactionsRepository = transactionsRepository;
  }

  @Override
  @Transactional
  public WalletsEntity ensureWallet(UUID ownerUserId) {
    Optional<WalletsEntity> existing = walletsRepository.findByOwnerUserId(ownerUserId);
    if (existing.isPresent()) {
      return existing.get();
    }
    try {
      WalletsEntity wallet = new WalletsEntity();
      wallet.setOwnerUserId(ownerUserId);
      wallet.setCurrency("INR");
      wallet.setAvailablePaise(0);
      wallet.setPendingPaise(0);
      wallet.setLockedPaise(0);
      return walletsRepository.saveAndFlush(wallet);
    } catch (DataIntegrityViolationException e) {
      // Race condition: another transaction created the wallet concurrently
      return walletsRepository
          .findByOwnerUserId(ownerUserId)
          .orElseThrow(
              () -> new ApiException(HttpStatus.INTERNAL_SERVER_ERROR, "WALLET_CREATE_FAILED"));
    }
  }

  @Override
  @Transactional
  public TransactionsEntity credit(WalletMutationRequest request) {
    if (request.getAmountPaise() <= 0) {
      throw new ApiException(HttpStatus.BAD_REQUEST, "INVALID_AMOUNT");
    }

    // Idempotency check
    Optional<TransactionsEntity> existing =
        transactionsRepository.findByIdempotencyKey(request.getIdempotencyKey());
    if (existing.isPresent()) {
      TransactionsEntity prior = existing.get();
      if (prior.getType() != request.getType()) {
        throw new ApiException(HttpStatus.CONFLICT, "IDEMPOTENCY_CONFLICT");
      }
      if (!prior.getAmountPaise().equals(request.getAmountPaise())) {
        throw new ApiException(HttpStatus.CONFLICT, "IDEMPOTENCY_CONFLICT");
      }
      return prior;
    }

    WalletsEntity wallet = ensureWallet(request.getOwnerUserId());

    int updated =
        walletsRepository.creditAvailable(
            wallet.getId(), request.getAmountPaise(), maxBalancePaise);
    if (updated == 0) {
      throw new ApiException(HttpStatus.CONFLICT, "BALANCE_LIMIT_EXCEEDED");
    }

    return transactionsRepository.save(buildTxn(request, wallet.getId()));
  }

  @Override
  @Transactional
  public TransactionsEntity debit(WalletMutationRequest request) {
    if (request.getAmountPaise() <= 0) {
      throw new ApiException(HttpStatus.BAD_REQUEST, "INVALID_AMOUNT");
    }

    // Idempotency check
    Optional<TransactionsEntity> existing =
        transactionsRepository.findByIdempotencyKey(request.getIdempotencyKey());
    if (existing.isPresent()) {
      TransactionsEntity prior = existing.get();
      if (prior.getType() != request.getType()) {
        throw new ApiException(HttpStatus.CONFLICT, "IDEMPOTENCY_CONFLICT");
      }
      return prior;
    }

    WalletsEntity wallet =
        walletsRepository
            .findByOwnerUserId(request.getOwnerUserId())
            .filter(w -> !Boolean.TRUE.equals(w.getIsDeleted()))
            .orElseThrow(() -> new ApiException(HttpStatus.NOT_FOUND, "WALLET_NOT_FOUND"));

    int updated = walletsRepository.debitAvailable(wallet.getId(), request.getAmountPaise());
    if (updated == 0) {
      throw new ApiException(HttpStatus.CONFLICT, "INSUFFICIENT_FUNDS");
    }

    return transactionsRepository.save(buildTxn(request, wallet.getId()));
  }

  private TransactionsEntity buildTxn(WalletMutationRequest request, UUID walletId) {
    TransactionsEntity txn = new TransactionsEntity();
    txn.setIdempotencyKey(request.getIdempotencyKey());
    txn.setType(request.getType());
    txn.setStatus(TransactionStatus.completed);
    txn.setAmountPaise(request.getAmountPaise());
    txn.setCurrency("INR");
    txn.setWalletId(walletId);
    txn.setFromUserId(request.getFromUserId());
    txn.setToUserId(request.getToUserId());
    txn.setOrderId(request.getOrderId());
    txn.setCampaignId(request.getCampaignId());
    txn.setPayoutId(request.getPayoutId());
    txn.setMetadata(request.getMetadata());
    return txn;
  }
}
