package com.mobo.wallet.service.impl;

import com.mobo.shared.enums.PayoutStatus;
import com.mobo.shared.exception.ApiException;
import com.mobo.wallet.api.PayoutsResponseDto;
import com.mobo.wallet.api.WalletAdminPort;
import com.mobo.wallet.mapper.PayoutsMapper;
import com.mobo.wallet.persistence.PayoutsEntity;
import com.mobo.wallet.persistence.PayoutsRepository;
import com.mobo.wallet.persistence.WalletsEntity;
import com.mobo.wallet.persistence.WalletsRepository;
import java.time.Instant;
import java.util.UUID;
import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
public class WalletAdminAdapter implements WalletAdminPort {

  private final WalletsRepository walletsRepository;
  private final PayoutsRepository payoutsRepository;
  private final PayoutsMapper payoutsMapper;

  public WalletAdminAdapter(
      WalletsRepository walletsRepository,
      PayoutsRepository payoutsRepository,
      PayoutsMapper payoutsMapper) {
    this.walletsRepository = walletsRepository;
    this.payoutsRepository = payoutsRepository;
    this.payoutsMapper = payoutsMapper;
  }

  @Override
  @Transactional
  public PayoutsResponseDto createPayout(
      UUID beneficiaryUserId, int amountPaise, UUID actorUserId) {
    WalletsEntity wallet =
        walletsRepository
            .findByOwnerUserId(beneficiaryUserId)
            .orElseThrow(() -> new ApiException(HttpStatus.NOT_FOUND, "WALLET_NOT_FOUND"));

    PayoutsEntity payout = new PayoutsEntity();
    payout.setBeneficiaryUserId(beneficiaryUserId);
    payout.setWalletId(wallet.getId());
    payout.setAmountPaise(amountPaise);
    payout.setCurrency("INR");
    payout.setStatus(PayoutStatus.recorded);
    payout.setRequestedAt(Instant.now());
    payout.setCreatedBy(actorUserId);
    payout.setUpdatedBy(actorUserId);

    PayoutsEntity saved = payoutsRepository.save(payout);
    return payoutsMapper.toResponse(saved);
  }

  @Override
  @Transactional
  public void deletePayout(UUID payoutId, UUID actorUserId) {
    PayoutsEntity payout =
        payoutsRepository
            .findById(payoutId)
            .filter(p -> !Boolean.TRUE.equals(p.getIsDeleted()))
            .orElseThrow(() -> new ApiException(HttpStatus.NOT_FOUND, "PAYOUT_NOT_FOUND"));

    if (payout.getStatus() == PayoutStatus.paid || payout.getStatus() == PayoutStatus.processing) {
      throw new ApiException(HttpStatus.CONFLICT, "PAYOUT_NOT_DELETABLE");
    }

    payout.setIsDeleted(true);
    payout.setUpdatedBy(actorUserId);
    payoutsRepository.save(payout);
  }
}
