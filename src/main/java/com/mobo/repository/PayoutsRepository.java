package com.mobo.repository;

import com.mobo.entity.PayoutsEntity;
import java.util.UUID;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;

public interface PayoutsRepository extends JpaRepository<PayoutsEntity, UUID> {

  Page<PayoutsEntity> findAllByIsDeletedFalse(Pageable pageable);

  Page<PayoutsEntity> findAllByBeneficiaryUserIdAndIsDeletedFalse(
      UUID beneficiaryUserId, Pageable pageable);
}
