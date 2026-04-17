package com.mobo.repository;

import com.mobo.entity.TransactionsEntity;
import java.util.Optional;
import java.util.UUID;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;

public interface TransactionsRepository extends JpaRepository<TransactionsEntity, UUID> {

  Page<TransactionsEntity> findAllByIsDeletedFalse(Pageable pageable);

  Optional<TransactionsEntity> findByIdempotencyKey(String idempotencyKey);

  Page<TransactionsEntity> findAllByWalletIdAndIsDeletedFalse(UUID walletId, Pageable pageable);
}
