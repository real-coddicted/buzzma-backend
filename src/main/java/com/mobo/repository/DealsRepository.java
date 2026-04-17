package com.mobo.repository;

import com.mobo.entity.DealsEntity;
import java.util.List;
import java.util.Optional;
import java.util.UUID;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;

public interface DealsRepository extends JpaRepository<DealsEntity, UUID> {

  Page<DealsEntity> findAllByIsDeletedFalse(Pageable pageable);

  Optional<DealsEntity> findByCampaignIdAndMediatorCode(UUID campaignId, String mediatorCode);

  Page<DealsEntity> findAllByMediatorCodeAndIsDeletedFalse(String mediatorCode, Pageable pageable);

  List<DealsEntity> findAllByMediatorCodeAndIsDeletedFalseAndActiveTrue(String mediatorCode);

  Page<DealsEntity> findAllByMediatorCodeInAndIsDeletedFalse(
      List<String> mediatorCodes, Pageable pageable);
}
