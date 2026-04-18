package com.coddicted.buzzma.wallet.service.impl;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyInt;
import static org.mockito.ArgumentMatchers.anyLong;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

import com.coddicted.buzzma.shared.enums.TransactionType;
import com.coddicted.buzzma.shared.exception.ApiException;
import com.coddicted.buzzma.wallet.api.TransactionsResponseDto;
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
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.http.HttpStatus;

@ExtendWith(MockitoExtension.class)
class WalletBusinessServiceImplTest {

  @Mock private WalletsRepository walletsRepository;
  @Mock private TransactionsRepository transactionsRepository;
  @Mock private WalletsMapper walletsMapper;
  @Mock private TransactionsMapper transactionsMapper;
  @InjectMocks private WalletBusinessServiceImpl service;

  // ── ensureWallet ────────────────────────────────────────────────────────

  @Test
  void ensureWallet_existingWallet_returnsExisting() {
    UUID userId = UUID.randomUUID();
    WalletsEntity wallet = new WalletsEntity();
    wallet.setOwnerUserId(userId);
    WalletsResponseDto dto = WalletsResponseDto.builder().ownerUserId(userId).build();
    when(walletsRepository.findByOwnerUserId(userId)).thenReturn(Optional.of(wallet));
    when(walletsMapper.toResponse(wallet)).thenReturn(dto);

    WalletsResponseDto result = service.ensureWallet(userId);

    assertThat(result).isEqualTo(dto);
    verify(walletsRepository, never()).saveAndFlush(any());
  }

  @Test
  void ensureWallet_noWallet_createsAndReturnsNew() {
    UUID userId = UUID.randomUUID();
    WalletsEntity saved = new WalletsEntity();
    saved.setOwnerUserId(userId);
    WalletsResponseDto dto = WalletsResponseDto.builder().ownerUserId(userId).build();
    when(walletsRepository.findByOwnerUserId(userId)).thenReturn(Optional.empty());
    when(walletsRepository.saveAndFlush(any())).thenReturn(saved);
    when(walletsMapper.toResponse(saved)).thenReturn(dto);

    WalletsResponseDto result = service.ensureWallet(userId);

    verify(walletsRepository).saveAndFlush(any());
    assertThat(result.getOwnerUserId()).isEqualTo(userId);
  }

  // ── credit ──────────────────────────────────────────────────────────────

  @Test
  void credit_idempotent_returnsPriorTxn() {
    UUID userId = UUID.randomUUID();
    String key = "idem-key-1";

    TransactionsEntity prior = new TransactionsEntity();
    prior.setIdempotencyKey(key);
    prior.setType(TransactionType.brand_deposit);
    prior.setAmountPaise(1000);
    TransactionsResponseDto priorDto =
        TransactionsResponseDto.builder().idempotencyKey(key).build();

    WalletMutationRequest req =
        WalletMutationRequest.builder()
            .idempotencyKey(key)
            .type(TransactionType.brand_deposit)
            .ownerUserId(userId)
            .amountPaise(1000)
            .build();

    when(transactionsRepository.findByIdempotencyKey(key)).thenReturn(Optional.of(prior));
    when(transactionsMapper.toResponse(prior)).thenReturn(priorDto);

    TransactionsResponseDto result = service.credit(req);

    assertThat(result).isEqualTo(priorDto);
    verify(walletsRepository, never()).creditAvailable(any(), anyInt(), anyLong());
  }

  @Test
  void credit_balanceLimitExceeded_throwsConflict() {
    UUID userId = UUID.randomUUID();
    UUID walletId = UUID.randomUUID();
    WalletsEntity wallet = new WalletsEntity();
    wallet.setId(walletId);
    wallet.setOwnerUserId(userId);

    WalletMutationRequest req =
        WalletMutationRequest.builder()
            .idempotencyKey("key-2")
            .type(TransactionType.brand_deposit)
            .ownerUserId(userId)
            .amountPaise(500)
            .build();

    when(transactionsRepository.findByIdempotencyKey("key-2")).thenReturn(Optional.empty());
    when(walletsRepository.findByOwnerUserId(userId)).thenReturn(Optional.of(wallet));
    when(walletsRepository.creditAvailable(eq(walletId), eq(500), anyLong())).thenReturn(0);

    assertThatThrownBy(() -> service.credit(req))
        .isInstanceOf(ApiException.class)
        .satisfies(ex -> assertThat(((ApiException) ex).getStatus()).isEqualTo(HttpStatus.CONFLICT))
        .satisfies(
            ex -> assertThat(((ApiException) ex).getCode()).isEqualTo("BALANCE_LIMIT_EXCEEDED"));
  }

  @Test
  void credit_success_savesTransaction() {
    UUID userId = UUID.randomUUID();
    UUID walletId = UUID.randomUUID();
    WalletsEntity wallet = new WalletsEntity();
    wallet.setId(walletId);
    wallet.setOwnerUserId(userId);
    TransactionsEntity saved = new TransactionsEntity();
    TransactionsResponseDto savedDto =
        TransactionsResponseDto.builder().idempotencyKey("key-3").build();

    WalletMutationRequest req =
        WalletMutationRequest.builder()
            .idempotencyKey("key-3")
            .type(TransactionType.cashback_settle)
            .ownerUserId(userId)
            .amountPaise(200)
            .build();

    when(transactionsRepository.findByIdempotencyKey("key-3")).thenReturn(Optional.empty());
    when(walletsRepository.findByOwnerUserId(userId)).thenReturn(Optional.of(wallet));
    when(walletsRepository.creditAvailable(eq(walletId), eq(200), anyLong())).thenReturn(1);
    when(transactionsRepository.save(any())).thenReturn(saved);
    when(transactionsMapper.toResponse(saved)).thenReturn(savedDto);

    TransactionsResponseDto result = service.credit(req);

    verify(transactionsRepository).save(any());
    assertThat(result).isEqualTo(savedDto);
  }

  // ── debit ───────────────────────────────────────────────────────────────

  @Test
  void debit_idempotent_returnsPriorTxn() {
    UUID userId = UUID.randomUUID();
    String key = "debit-idem-1";
    TransactionsEntity prior = new TransactionsEntity();
    prior.setIdempotencyKey(key);
    prior.setType(TransactionType.order_settlement_debit);
    TransactionsResponseDto priorDto =
        TransactionsResponseDto.builder().idempotencyKey(key).build();

    WalletMutationRequest req =
        WalletMutationRequest.builder()
            .idempotencyKey(key)
            .type(TransactionType.order_settlement_debit)
            .ownerUserId(userId)
            .amountPaise(300)
            .build();

    when(transactionsRepository.findByIdempotencyKey(key)).thenReturn(Optional.of(prior));
    when(transactionsMapper.toResponse(prior)).thenReturn(priorDto);

    TransactionsResponseDto result = service.debit(req);

    assertThat(result).isEqualTo(priorDto);
    verify(walletsRepository, never()).debitAvailable(any(), anyInt());
  }

  @Test
  void debit_insufficientFunds_throwsConflict() {
    UUID userId = UUID.randomUUID();
    UUID walletId = UUID.randomUUID();
    WalletsEntity wallet = new WalletsEntity();
    wallet.setId(walletId);
    wallet.setOwnerUserId(userId);
    wallet.setIsDeleted(false);

    WalletMutationRequest req =
        WalletMutationRequest.builder()
            .idempotencyKey("debit-key-2")
            .type(TransactionType.order_settlement_debit)
            .ownerUserId(userId)
            .amountPaise(999_999)
            .build();

    when(transactionsRepository.findByIdempotencyKey("debit-key-2")).thenReturn(Optional.empty());
    when(walletsRepository.findByOwnerUserId(userId)).thenReturn(Optional.of(wallet));
    when(walletsRepository.debitAvailable(walletId, 999_999)).thenReturn(0);

    assertThatThrownBy(() -> service.debit(req))
        .isInstanceOf(ApiException.class)
        .satisfies(ex -> assertThat(((ApiException) ex).getStatus()).isEqualTo(HttpStatus.CONFLICT))
        .satisfies(ex -> assertThat(((ApiException) ex).getCode()).isEqualTo("INSUFFICIENT_FUNDS"));
  }
}
