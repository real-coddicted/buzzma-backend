package com.mobo.wallet.persistence;

import java.util.Optional;
import java.util.UUID;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Modifying;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

public interface WalletsRepository extends JpaRepository<WalletsEntity, UUID> {

  Page<WalletsEntity> findAllByIsDeletedFalse(Pageable pageable);

  Optional<WalletsEntity> findByOwnerUserId(UUID ownerUserId);

  @Modifying
  @Query(
      "UPDATE WalletsEntity w SET w.availablePaise = w.availablePaise + :amount"
          + " WHERE w.id = :walletId"
          + " AND (w.availablePaise + w.lockedPaise + w.pendingPaise + :amount) <= :maxBalance")
  int creditAvailable(
      @Param("walletId") UUID walletId,
      @Param("amount") int amount,
      @Param("maxBalance") long maxBalance);

  @Modifying
  @Query(
      "UPDATE WalletsEntity w SET w.availablePaise = w.availablePaise - :amount"
          + " WHERE w.id = :walletId AND w.availablePaise >= :amount")
  int debitAvailable(@Param("walletId") UUID walletId, @Param("amount") int amount);
}
