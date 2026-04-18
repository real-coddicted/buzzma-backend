package com.mobo.orders.api;

import com.mobo.orders.persistence.OrdersEntity;
import com.mobo.shared.enums.OrderWorkflowStatus;
import java.util.UUID;

public interface OrderWorkflowService {

  void assertTransition(OrderWorkflowStatus from, OrderWorkflowStatus to);

  OrdersEntity transition(
      UUID orderId, OrderWorkflowStatus from, OrderWorkflowStatus to, UUID actorUserId);

  int freezeByUserId(UUID userId, String reason, UUID actorUserId);

  OrdersEntity reactivate(UUID orderId, UUID actorUserId, String reason);
}
