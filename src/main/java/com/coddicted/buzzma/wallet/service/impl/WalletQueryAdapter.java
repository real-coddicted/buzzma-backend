package com.coddicted.buzzma.wallet.service.impl;

import com.coddicted.buzzma.shared.common.OffsetBasedPageRequest;
import com.coddicted.buzzma.shared.exception.ApiException;
import com.coddicted.buzzma.wallet.api.PayoutsResponseDto;
import com.coddicted.buzzma.wallet.api.TransactionsResponseDto;
import com.coddicted.buzzma.wallet.api.WalletQueryPort;
import com.coddicted.buzzma.wallet.api.WalletsResponseDto;
import com.coddicted.buzzma.wallet.mapper.PayoutsMapper;
import com.coddicted.buzzma.wallet.mapper.TransactionsMapper;
import com.coddicted.buzzma.wallet.mapper.WalletsMapper;
import com.coddicted.buzzma.wallet.persistence.PayoutsRepository;
import com.coddicted.buzzma.wallet.persistence.TransactionsRepository;
import com.coddicted.buzzma.wallet.persistence.WalletsEntity;
import com.coddicted.buzzma.wallet.persistence.WalletsRepository;
import java.util.List;
import java.util.Optional;
import java.util.UUID;
import java.util.stream.Collectors;
import org.springframework.data.domain.Sort;
import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
public class WalletQueryAdapter implements WalletQueryPort {

  private final WalletsRepository walletsRepository;
  private final TransactionsRepository transactionsRepository;
  private final PayoutsRepository payoutsRepository;
  private final WalletsMapper walletsMapper;
  private final TransactionsMapper transactionsMapper;
  private final PayoutsMapper payoutsMapper;

  public WalletQueryAdapter(
      WalletsRepository walletsRepository,
      TransactionsRepository transactionsRepository,
      PayoutsRepository payoutsRepository,
      WalletsMapper walletsMapper,
      TransactionsMapper transactionsMapper,
      PayoutsMapper payoutsMapper) {
    this.walletsRepository = walletsRepository;
    this.transactionsRepository = transactionsRepository;
    this.payoutsRepository = payoutsRepository;
    this.walletsMapper = walletsMapper;
    this.transactionsMapper = transactionsMapper;
    this.payoutsMapper = payoutsMapper;
  }

  @Override
  @Transactional(readOnly = true)
  public Optional<WalletsResponseDto> findByOwnerUserId(UUID ownerUserId) {
    return walletsRepository.findByOwnerUserId(ownerUserId).map(walletsMapper::toResponse);
  }

  @Override
  @Transactional(readOnly = true)
  public List<TransactionsResponseDto> listLedgerByOwnerUserId(
      UUID ownerUserId, int limit, int offset) {
    WalletsEntity wallet =
        walletsRepository
            .findByOwnerUserId(ownerUserId)
            .orElseThrow(() -> new ApiException(HttpStatus.NOT_FOUND, "WALLET_NOT_FOUND"));

    var pageable =
        new OffsetBasedPageRequest(limit, offset, Sort.by(Sort.Direction.DESC, "createdAt"));
    return transactionsRepository
        .findAllByWalletIdAndIsDeletedFalse(wallet.getId(), pageable)
        .stream()
        .map(transactionsMapper::toResponse)
        .collect(Collectors.toList());
  }

  @Override
  @Transactional(readOnly = true)
  public List<PayoutsResponseDto> listPayouts(int limit, int offset) {
    var pageable =
        new OffsetBasedPageRequest(limit, offset, Sort.by(Sort.Direction.DESC, "requestedAt"));
    return payoutsRepository.findAllByIsDeletedFalse(pageable).stream()
        .map(payoutsMapper::toResponse)
        .collect(Collectors.toList());
  }
}
