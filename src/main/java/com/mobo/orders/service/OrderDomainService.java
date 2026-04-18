package com.mobo.orders.service;

import com.mobo.admin.api.AuditLogsResponseDto;
import com.mobo.orders.api.OrdersResponseDto;
import java.util.List;
import java.util.Map;
import java.util.UUID;

public interface OrderDomainService {

  List<OrdersResponseDto> getUserOrders(UUID userId, int limit, int offset);

  OrdersResponseDto createOrder(
      UUID userId,
      UUID brandUserId,
      int totalPaise,
      String externalOrderId,
      String managerName,
      String agencyName,
      String buyerName,
      String buyerMobile,
      String brandName,
      String settlementMode,
      Integer returnWindowDays,
      UUID actorUserId);

  OrdersResponseDto submitClaim(
      UUID orderId, String screenshotOrder, String screenshotPayment, UUID actorUserId);

  OrdersResponseDto setReviewerName(UUID orderId, String reviewerName, UUID actorUserId);

  String getOrderProof(UUID orderId, String type, UUID actorUserId);

  Map<String, String> getSignedProofUrls(UUID orderId, UUID actorUserId);

  List<AuditLogsResponseDto> getOrderAudit(
      UUID orderId, UUID actorUserId, String role, int limit, int offset);
}
