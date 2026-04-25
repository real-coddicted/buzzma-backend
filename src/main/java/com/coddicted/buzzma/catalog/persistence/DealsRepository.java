package com.coddicted.buzzma.catalog.persistence;

import java.util.List;
import java.util.Optional;
import java.util.UUID;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

public interface DealsRepository extends JpaRepository<DealsEntity, UUID> {

  Page<DealsEntity> findAllByIsDeletedFalse(Pageable pageable);

  Optional<DealsEntity> findByCampaignIdAndMediatorCode(UUID campaignId, String mediatorCode);

  Page<DealsEntity> findAllByMediatorCodeAndIsDeletedFalse(String mediatorCode, Pageable pageable);

  List<DealsEntity> findAllByMediatorCodeAndIsDeletedFalseAndActiveTrue(String mediatorCode);

  Page<DealsEntity> findAllByMediatorCodeInAndIsDeletedFalse(
      List<String> mediatorCodes, Pageable pageable);

  @Query(
      value =
          "SELECT d.* FROM deals d"
              + " JOIN campaigns c ON d.campaign_id = c.id"
              + " WHERE d.mediator_code = :mediatorCode"
              + " AND d.active = true AND d.is_deleted = false"
              + " AND c.is_deleted = false AND c.status = 'active'"
              + " ORDER BY d.created_at DESC",
      countQuery =
          "SELECT COUNT(*) FROM deals d"
              + " JOIN campaigns c ON d.campaign_id = c.id"
              + " WHERE d.mediator_code = :mediatorCode"
              + " AND d.active = true AND d.is_deleted = false"
              + " AND c.is_deleted = false AND c.status = 'active'",
      nativeQuery = true)
  Page<DealsEntity> findActiveProductsForMediator(
      @Param("mediatorCode") String mediatorCode, Pageable pageable);
}
