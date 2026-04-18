package com.coddicted.buzzma.wallet.service.impl;

import com.coddicted.buzzma.shared.enums.TransactionStatus;
import com.coddicted.buzzma.shared.exception.ApiException;
import com.coddicted.buzzma.wallet.api.TransactionsResponseDto;
import com.coddicted.buzzma.wallet.api.WalletBusinessService;
import com.coddicted.buzzma.wallet.api.WalletMutationRequest;
import com.coddicted.buzzma.wallet.api.WalletsResponseDto;
import com.coddicted.buzzma.wallet.mapper.TransactionsMapper;
import com.coddicted.buzzma.wallet.mapper.WalletsMapper;
import com.coddicted.buzzma.wallet.persistence.TransactionsEntity;
import com.coddicted.buzzma.wallet.persistence.TransactionsRepository;
import com.coddicted.buzzma.wallet.persistence.WalletsEntity;
import com.coddicted.buzzma.wallet.persistence.WalletsRepository;
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
  private final WalletsMapper walletsMapper;
  private final TransactionsMapper transactionsMapper;

  public WalletBusinessServiceImpl(
      WalletsRepository walletsRepository,
      TransactionsRepository transactionsRepository,
      WalletsMapper walletsMapper,
      TransactionsMapper transactionsMapper) {
    this.walletsRepository = walletsRepository;
    this.transactionsRepository = transactionsRepository;
    this.walletsMapper = walletsMapper;
    this.transactionsMapper = transactionsMapper;
  }

  @Override
  @Transactional
  public WalletsResponseDto ensureWallet(UUID ownerUserId) {
    return walletsMapper.toResponse(ensureWalletEntity(ownerUserId));
  }

  @Override
  @Transactional
  public TransactionsResponseDto credit(WalletMutationRequest request) {
    if (request.getAmountPaise() <= 0) {
      throw new ApiException(HttpStatus.BAD_REQUEST, "INVALID_AMOUNT");
    }

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
      return transactionsMapper.toResponse(prior);
    }

    WalletsEntity wallet = ensureWalletEntity(request.getOwnerUserId());

    int updated =
        walletsRepository.creditAvailable(
            wallet.getId(), request.getAmountPaise(), maxBalancePaise);
    if (updated == 0) {
      throw new ApiException(HttpStatus.CONFLICT, "BALANCE_LIMIT_EXCEEDED");
    }

    return transactionsMapper.toResponse(
        transactionsRepository.save(buildTxn(request, wallet.getId())));
  }

  @Override
  @Transactional
  public TransactionsResponseDto debit(WalletMutationRequest request) {
    if (request.getAmountPaise() <= 0) {
      throw new ApiException(HttpStatus.BAD_REQUEST, "INVALID_AMOUNT");
    }

    Optional<TransactionsEntity> existing =
        transactionsRepository.findByIdempotencyKey(request.getIdempotencyKey());
    if (existing.isPresent()) {
      TransactionsEntity prior = existing.get();
      if (prior.getType() != request.getType()) {
        throw new ApiException(HttpStatus.CONFLICT, "IDEMPOTENCY_CONFLICT");
      }
      return transactionsMapper.toResponse(prior);
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

    return transactionsMapper.toResponse(
        transactionsRepository.save(buildTxn(request, wallet.getId())));
  }

  private WalletsEntity ensureWalletEntity(UUID ownerUserId) {
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
      return walletsRepository
          .findByOwnerUserId(ownerUserId)
          .orElseThrow(
              () -> new ApiException(HttpStatus.INTERNAL_SERVER_ERROR, "WALLET_CREATE_FAILED"));
    }
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
