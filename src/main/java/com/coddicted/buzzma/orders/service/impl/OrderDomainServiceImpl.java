package com.coddicted.buzzma.orders.service.impl;

import com.coddicted.buzzma.admin.api.AuditLogQueryPort;
import com.coddicted.buzzma.admin.api.AuditLogsResponseDto;
import com.coddicted.buzzma.orders.api.OrderWorkflowService;
import com.coddicted.buzzma.orders.api.OrdersResponseDto;
import com.coddicted.buzzma.orders.mapper.OrdersMapper;
import com.coddicted.buzzma.orders.persistence.OrdersEntity;
import com.coddicted.buzzma.orders.persistence.OrdersRepository;
import com.coddicted.buzzma.orders.service.OrderDomainService;
import com.coddicted.buzzma.shared.common.AuditLogWriter;
import com.coddicted.buzzma.shared.common.OffsetBasedPageRequest;
import com.coddicted.buzzma.shared.enums.OrderWorkflowStatus;
import com.coddicted.buzzma.shared.enums.SettlementMode;
import com.coddicted.buzzma.shared.exception.ApiException;
import com.coddicted.buzzma.wallet.api.WalletBusinessService;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.UUID;
import java.util.stream.Collectors;
import org.springframework.data.domain.Sort;
import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
public class OrderDomainServiceImpl implements OrderDomainService {

  private final OrdersRepository ordersRepository;
  private final AuditLogQueryPort auditLogQueryPort;
  private final OrderWorkflowService orderWorkflowService;
  private final WalletBusinessService walletBusinessService;
  private final OrdersMapper ordersMapper;
  private final AuditLogWriter auditLogWriter;

  public OrderDomainServiceImpl(
      OrdersRepository ordersRepository,
      AuditLogQueryPort auditLogQueryPort,
      OrderWorkflowService orderWorkflowService,
      WalletBusinessService walletBusinessService,
      OrdersMapper ordersMapper,
      AuditLogWriter auditLogWriter) {
    this.ordersRepository = ordersRepository;
    this.auditLogQueryPort = auditLogQueryPort;
    this.orderWorkflowService = orderWorkflowService;
    this.walletBusinessService = walletBusinessService;
    this.ordersMapper = ordersMapper;
    this.auditLogWriter = auditLogWriter;
  }

  @Override
  @Transactional(readOnly = true)
  public List<OrdersResponseDto> getUserOrders(UUID userId, int limit, int offset) {
    var pageable =
        new OffsetBasedPageRequest(limit, offset, Sort.by(Sort.Direction.DESC, "createdAt"));
    return ordersRepository.findAllByUserIdAndIsDeletedFalse(userId, pageable).stream()
        .map(ordersMapper::toResponse)
        .collect(Collectors.toList());
  }

  @Override
  @Transactional
  public OrdersResponseDto createOrder(
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
      UUID actorUserId) {

    if (externalOrderId != null && !externalOrderId.isBlank()) {
      ordersRepository
          .findByExternalOrderIdAndIsDeletedFalse(externalOrderId)
          .ifPresent(
              o -> {
                throw new ApiException(HttpStatus.CONFLICT, "EXTERNAL_ORDER_ID_EXISTS");
              });
    }

    OrdersEntity order = new OrdersEntity();
    order.setUserId(userId);
    order.setBrandUserId(brandUserId);
    order.setTotalPaise(totalPaise);
    order.setExternalOrderId(externalOrderId);
    order.setManagerName(managerName);
    order.setAgencyName(agencyName);
    order.setBuyerName(buyerName);
    order.setBuyerMobile(buyerMobile);
    order.setBrandName(brandName);
    if (returnWindowDays != null) {
      order.setReturnWindowDays(returnWindowDays);
    }
    if (settlementMode != null) {
      try {
        order.setSettlementMode(SettlementMode.valueOf(settlementMode));
      } catch (IllegalArgumentException ignored) {
        // default kept
      }
    }
    order.setWorkflowStatus(OrderWorkflowStatus.CREATED);
    order.setCreatedBy(actorUserId);
    order.setUpdatedBy(actorUserId);

    OrdersEntity saved = ordersRepository.save(order);

    // ensure wallet exists for the user
    walletBusinessService.ensureWallet(userId);

    // transition CREATED → REDIRECTED
    saved =
        orderWorkflowService.transition(
            saved.getId(),
            OrderWorkflowStatus.CREATED,
            OrderWorkflowStatus.REDIRECTED,
            actorUserId);

    auditLogWriter.write(
        actorUserId,
        new String[] {"shopper"},
        "CREATE_ORDER",
        "Order",
        saved.getId().toString(),
        null);

    return ordersMapper.toResponse(saved);
  }

  @Override
  @Transactional
  public OrdersResponseDto submitClaim(
      UUID orderId, String screenshotOrder, String screenshotPayment, UUID actorUserId) {
    OrdersEntity order =
        ordersRepository
            .findById(orderId)
            .filter(o -> !Boolean.TRUE.equals(o.getIsDeleted()))
            .orElseThrow(() -> new ApiException(HttpStatus.NOT_FOUND, "ORDER_NOT_FOUND"));

    order.setScreenshotOrder(screenshotOrder);
    order.setScreenshotPayment(screenshotPayment);
    order.setUpdatedBy(actorUserId);
    ordersRepository.save(order);

    OrdersEntity transitioned =
        orderWorkflowService.transition(
            orderId, OrderWorkflowStatus.ORDERED, OrderWorkflowStatus.PROOF_SUBMITTED, actorUserId);

    auditLogWriter.write(
        actorUserId, new String[] {"shopper"}, "SUBMIT_CLAIM", "Order", orderId.toString(), null);

    return ordersMapper.toResponse(transitioned);
  }

  @Override
  @Transactional
  public OrdersResponseDto setReviewerName(UUID orderId, String reviewerName, UUID actorUserId) {
    OrdersEntity order =
        ordersRepository
            .findById(orderId)
            .filter(o -> !Boolean.TRUE.equals(o.getIsDeleted()))
            .orElseThrow(() -> new ApiException(HttpStatus.NOT_FOUND, "ORDER_NOT_FOUND"));

    order.setReviewerName(reviewerName);
    order.setUpdatedBy(actorUserId);
    return ordersMapper.toResponse(ordersRepository.save(order));
  }

  @Override
  @Transactional(readOnly = true)
  public String getOrderProof(UUID orderId, String type, UUID actorUserId) {
    OrdersEntity order =
        ordersRepository
            .findById(orderId)
            .filter(o -> !Boolean.TRUE.equals(o.getIsDeleted()))
            .orElseThrow(() -> new ApiException(HttpStatus.NOT_FOUND, "ORDER_NOT_FOUND"));

    return switch (type) {
      case "order" -> order.getScreenshotOrder();
      case "payment" -> order.getScreenshotPayment();
      case "review" -> order.getScreenshotReview();
      case "rating" -> order.getScreenshotRating();
      case "returnWindow" -> order.getScreenshotReturnWindow();
      default -> throw new ApiException(HttpStatus.BAD_REQUEST, "INVALID_PROOF_TYPE");
    };
  }

  @Override
  @Transactional(readOnly = true)
  public Map<String, String> getSignedProofUrls(UUID orderId, UUID actorUserId) {
    OrdersEntity order =
        ordersRepository
            .findById(orderId)
            .filter(o -> !Boolean.TRUE.equals(o.getIsDeleted()))
            .orElseThrow(() -> new ApiException(HttpStatus.NOT_FOUND, "ORDER_NOT_FOUND"));

    Map<String, String> urls = new HashMap<>();
    if (order.getScreenshotOrder() != null) {
      urls.put("order", order.getScreenshotOrder());
    }
    if (order.getScreenshotPayment() != null) {
      urls.put("payment", order.getScreenshotPayment());
    }
    if (order.getScreenshotReview() != null) {
      urls.put("review", order.getScreenshotReview());
    }
    if (order.getScreenshotRating() != null) {
      urls.put("rating", order.getScreenshotRating());
    }
    if (order.getScreenshotReturnWindow() != null) {
      urls.put("returnWindow", order.getScreenshotReturnWindow());
    }
    return urls;
  }

  @Override
  @Transactional(readOnly = true)
  public List<AuditLogsResponseDto> getOrderAudit(
      UUID orderId, UUID actorUserId, String role, int limit, int offset) {
    ordersRepository
        .findById(orderId)
        .filter(o -> !Boolean.TRUE.equals(o.getIsDeleted()))
        .orElseThrow(() -> new ApiException(HttpStatus.NOT_FOUND, "ORDER_NOT_FOUND"));

    return auditLogQueryPort.listByEntity("Order", orderId.toString(), limit, offset);
  }
}
