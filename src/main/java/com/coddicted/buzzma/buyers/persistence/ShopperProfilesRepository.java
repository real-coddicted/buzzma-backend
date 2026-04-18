package com.coddicted.buzzma.buyers.persistence;

import java.util.Optional;
import java.util.UUID;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;

public interface ShopperProfilesRepository extends JpaRepository<ShopperProfilesEntity, UUID> {

  Page<ShopperProfilesEntity> findAllByIsDeletedFalse(Pageable pageable);

  Optional<ShopperProfilesEntity> findByUserId(UUID userId);
}
