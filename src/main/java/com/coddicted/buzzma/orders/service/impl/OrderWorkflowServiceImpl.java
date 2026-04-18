package com.coddicted.buzzma.orders.service.impl;

import com.coddicted.buzzma.orders.api.OrderWorkflowService;
import com.coddicted.buzzma.orders.persistence.OrdersEntity;
import com.coddicted.buzzma.orders.persistence.OrdersRepository;
import com.coddicted.buzzma.shared.enums.OrderWorkflowStatus;
import com.coddicted.buzzma.shared.exception.ApiException;
import java.time.Instant;
import java.util.EnumMap;
import java.util.Map;
import java.util.Set;
import java.util.UUID;
import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
public class OrderWorkflowServiceImpl implements OrderWorkflowService {

  private static final int DEFAULT_MAX_REPROOF_ATTEMPTS = 5;

  private static final Set<OrderWorkflowStatus> TERMINAL =
      Set.of(OrderWorkflowStatus.COMPLETED, OrderWorkflowStatus.FAILED);

  private static final Map<OrderWorkflowStatus, Set<OrderWorkflowStatus>> ALLOWED;

  static {
    ALLOWED = new EnumMap<>(OrderWorkflowStatus.class);
    ALLOWED.put(OrderWorkflowStatus.CREATED, Set.of(OrderWorkflowStatus.REDIRECTED));
    ALLOWED.put(OrderWorkflowStatus.REDIRECTED, Set.of(OrderWorkflowStatus.ORDERED));
    ALLOWED.put(OrderWorkflowStatus.ORDERED, Set.of(OrderWorkflowStatus.PROOF_SUBMITTED));
    ALLOWED.put(
        OrderWorkflowStatus.PROOF_SUBMITTED,
        Set.of(OrderWorkflowStatus.UNDER_REVIEW, OrderWorkflowStatus.ORDERED));
    ALLOWED.put(
        OrderWorkflowStatus.UNDER_REVIEW,
        Set.of(
            OrderWorkflowStatus.APPROVED,
            OrderWorkflowStatus.REJECTED,
            OrderWorkflowStatus.PROOF_SUBMITTED,
            OrderWorkflowStatus.ORDERED));
    ALLOWED.put(
        OrderWorkflowStatus.APPROVED,
        Set.of(OrderWorkflowStatus.REWARD_PENDING, OrderWorkflowStatus.ORDERED));
    ALLOWED.put(
        OrderWorkflowStatus.REJECTED,
        Set.of(OrderWorkflowStatus.FAILED, OrderWorkflowStatus.PROOF_SUBMITTED));
    ALLOWED.put(
        OrderWorkflowStatus.REWARD_PENDING,
        Set.of(OrderWorkflowStatus.COMPLETED, OrderWorkflowStatus.FAILED));
    ALLOWED.put(OrderWorkflowStatus.COMPLETED, Set.of());
    ALLOWED.put(OrderWorkflowStatus.FAILED, Set.of());
  }

  private final OrdersRepository ordersRepository;

  public OrderWorkflowServiceImpl(OrdersRepository ordersRepository) {
    this.ordersRepository = ordersRepository;
  }

  @Override
  public void assertTransition(OrderWorkflowStatus from, OrderWorkflowStatus to) {
    Set<OrderWorkflowStatus> allowed = ALLOWED.getOrDefault(from, Set.of());
    if (!allowed.contains(to)) {
      throw new ApiException(
          HttpStatus.CONFLICT,
          "ILLEGAL_ORDER_TRANSITION",
          "Illegal order transition: " + from + " -> " + to);
    }
  }

  @Override
  @Transactional
  public OrdersEntity transition(
      UUID orderId, OrderWorkflowStatus from, OrderWorkflowStatus to, UUID actorUserId) {
    assertTransition(from, to);

    OrdersEntity order =
        ordersRepository
            .findById(orderId)
            .filter(o -> !Boolean.TRUE.equals(o.getIsDeleted()))
            .orElseThrow(() -> new ApiException(HttpStatus.NOT_FOUND, "ORDER_NOT_FOUND"));

    if (Boolean.TRUE.equals(order.getFrozen())) {
      throw new ApiException(HttpStatus.CONFLICT, "ORDER_FROZEN");
    }
    if (order.getWorkflowStatus() != from) {
      throw new ApiException(HttpStatus.CONFLICT, "ORDER_STATE_MISMATCH");
    }

    // Rate-limit re-proof submissions
    if (from == OrderWorkflowStatus.REJECTED && to == OrderWorkflowStatus.PROOF_SUBMITTED) {
      String events = order.getEvents() != null ? order.getEvents() : "[]";
      int rejectionCount = countOccurrences(events, "\"to\":\"REJECTED\"");
      if (rejectionCount >= DEFAULT_MAX_REPROOF_ATTEMPTS) {
        throw new ApiException(HttpStatus.TOO_MANY_REQUESTS, "REPROOF_LIMIT_EXCEEDED");
      }
    }

    String newEvents = appendEvent(order.getEvents(), from, to, actorUserId);

    int updated = ordersRepository.transitionWorkflowStatus(orderId, from, to, newEvents);
    if (updated == 0) {
      throw new ApiException(HttpStatus.CONFLICT, "ORDER_STATE_MISMATCH");
    }

    return ordersRepository
        .findById(orderId)
        .orElseThrow(() -> new ApiException(HttpStatus.NOT_FOUND, "ORDER_NOT_FOUND"));
  }

  @Override
  @Transactional
  public int freezeByUserId(UUID userId, String reason, UUID actorUserId) {
    return ordersRepository.freezeByUserId(userId, Instant.now(), reason);
  }

  @Override
  @Transactional
  public OrdersEntity reactivate(UUID orderId, UUID actorUserId, String reason) {
    OrdersEntity order =
        ordersRepository
            .findById(orderId)
            .filter(o -> !Boolean.TRUE.equals(o.getIsDeleted()))
            .filter(o -> Boolean.TRUE.equals(o.getFrozen()))
            .orElseThrow(() -> new ApiException(HttpStatus.NOT_FOUND, "ORDER_NOT_FOUND"));

    String currentEvents = order.getEvents() != null ? order.getEvents() : "[]";
    String newEvent =
        String.format(
            "{\"type\":\"WORKFLOW_REACTIVATED\",\"at\":\"%s\",\"actorUserId\":\"%s\","
                + "\"metadata\":{\"reason\":\"%s\"}}",
            Instant.now(), actorUserId, reason != null ? reason : "");
    String newEvents = appendRaw(currentEvents, newEvent);

    int updated = ordersRepository.reactivateOrder(orderId, Instant.now(), actorUserId, newEvents);
    if (updated == 0) {
      throw new ApiException(HttpStatus.CONFLICT, "ORDER_REACTIVATION_FAILED");
    }

    return ordersRepository
        .findById(orderId)
        .orElseThrow(() -> new ApiException(HttpStatus.NOT_FOUND, "ORDER_NOT_FOUND"));
  }

  private String appendEvent(
      String existingEvents, OrderWorkflowStatus from, OrderWorkflowStatus to, UUID actorUserId) {
    String event =
        String.format(
            "{\"type\":\"WORKFLOW_TRANSITION\",\"at\":\"%s\",\"actorUserId\":\"%s\","
                + "\"metadata\":{\"from\":\"%s\",\"to\":\"%s\"}}",
            Instant.now(), actorUserId, from.name(), to.name());
    return appendRaw(existingEvents != null ? existingEvents : "[]", event);
  }

  private String appendRaw(String jsonArray, String newElement) {
    String trimmed = jsonArray.trim();
    if ("[]".equals(trimmed)) {
      return "[" + newElement + "]";
    }
    // Insert before the closing bracket
    return trimmed.substring(0, trimmed.length() - 1) + "," + newElement + "]";
  }

  private int countOccurrences(String text, String pattern) {
    int count = 0;
    int idx = 0;
    while ((idx = text.indexOf(pattern, idx)) != -1) {
      count++;
      idx += pattern.length();
    }
    return count;
  }
}
