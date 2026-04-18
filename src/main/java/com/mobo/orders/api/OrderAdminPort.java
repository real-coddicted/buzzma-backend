package com.mobo.orders.api;

import java.util.UUID;

public interface OrderAdminPort {

  OrdersResponseDto verifyOrderClaim(UUID orderId, UUID actorUserId);

  OrdersResponseDto verifyRequirement(UUID orderId, String type, UUID actorUserId);

  OrdersResponseDto verifyAllSteps(UUID orderId, UUID actorUserId);

  void rejectProof(UUID orderId, String type, String reason, UUID actorUserId);

  void cancelAllProofs(UUID orderId, String reason, UUID actorUserId);

  void requestMissingProof(UUID orderId, String type, String note, UUID actorUserId);

  void forceApprove(UUID orderId, UUID actorUserId);

  void cancelOrder(UUID orderId, String reason, UUID actorUserId);

  void settleOrderPayment(
      UUID orderId, String settlementRef, String settlementMode, UUID actorUserId);

  void unsettleOrderPayment(UUID orderId, UUID actorUserId);

  long countByCampaignId(UUID campaignId);
}
