package com.mobo.repository;

import com.mobo.entity.OrdersEntity;
import com.mobo.entity.enums.AffiliateStatus;
import com.mobo.entity.enums.OrderWorkflowStatus;
import java.time.Instant;
import java.util.List;
import java.util.Optional;
import java.util.UUID;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Modifying;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

@SuppressWarnings("checkstyle:MethodName")
public interface OrdersRepository extends JpaRepository<OrdersEntity, UUID> {

  Page<OrdersEntity> findAllByIsDeletedFalse(Pageable pageable);

  Optional<OrdersEntity> findByExternalOrderIdAndIsDeletedFalse(String externalOrderId);

  Page<OrdersEntity> findAllByUserIdAndIsDeletedFalse(UUID userId, Pageable pageable);

  Page<OrdersEntity> findAllByBrandUserIdAndIsDeletedFalse(UUID brandUserId, Pageable pageable);

  List<OrdersEntity> findAllByWorkflowStatusAndAffiliateStatusAndFrozenFalseAndIsDeletedFalse(
      OrderWorkflowStatus workflowStatus, AffiliateStatus affiliateStatus, Pageable pageable);

  @Query(
      "SELECT o FROM OrdersEntity o WHERE o.workflowStatus = :workflowStatus"
          + " AND o.affiliateStatus = :affiliateStatus"
          + " AND o.frozen = false"
          + " AND o.isDeleted = false"
          + " AND o.expectedSettlementDate <= :now")
  List<OrdersEntity> findSettleable(
      @Param("workflowStatus") OrderWorkflowStatus workflowStatus,
      @Param("affiliateStatus") AffiliateStatus affiliateStatus,
      @Param("now") Instant now);

  @Modifying
  @Query(
      "UPDATE OrdersEntity o SET o.workflowStatus = :to, o.events = :events"
          + " WHERE o.id = :id AND o.workflowStatus = :from"
          + " AND o.frozen = false AND o.isDeleted = false")
  int transitionWorkflowStatus(
      @Param("id") UUID id,
      @Param("from") OrderWorkflowStatus from,
      @Param("to") OrderWorkflowStatus to,
      @Param("events") String events);

  @Modifying
  @Query(
      "UPDATE OrdersEntity o SET o.frozen = true, o.frozenAt = :frozenAt,"
          + " o.frozenReason = :reason"
          + " WHERE o.userId = :userId AND o.frozen = false AND o.isDeleted = false")
  int freezeByUserId(
      @Param("userId") UUID userId,
      @Param("frozenAt") Instant frozenAt,
      @Param("reason") String reason);

  Page<OrdersEntity> findAllByManagerNameInAndIsDeletedFalse(
      List<String> managerNames, Pageable pageable);

  @Modifying
  @Query(
      "UPDATE OrdersEntity o SET o.frozen = false, o.reactivatedAt = :now,"
          + " o.reactivatedBy = :actorUserId, o.frozenReason = null, o.frozenAt = null,"
          + " o.events = :events"
          + " WHERE o.id = :id AND o.frozen = true AND o.isDeleted = false")
  int reactivateOrder(
      @Param("id") UUID id,
      @Param("now") Instant now,
      @Param("actorUserId") UUID actorUserId,
      @Param("events") String events);
}
