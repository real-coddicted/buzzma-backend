package com.mobo.orders.persistence;

import java.util.List;
import java.util.UUID;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;

public interface OrderItemsRepository extends JpaRepository<OrderItemsEntity, UUID> {

  Page<OrderItemsEntity> findAllByIsDeletedFalse(Pageable pageable);

  List<OrderItemsEntity> findAllByOrderIdAndIsDeletedFalse(UUID orderId);

  List<OrderItemsEntity> findAllByCampaignIdAndIsDeletedFalse(UUID campaignId);
}
