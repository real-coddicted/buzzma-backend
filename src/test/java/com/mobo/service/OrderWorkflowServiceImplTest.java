package com.mobo.service;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.when;

import com.mobo.entity.OrdersEntity;
import com.mobo.entity.enums.OrderWorkflowStatus;
import com.mobo.exception.ApiException;
import com.mobo.repository.OrdersRepository;
import com.mobo.service.impl.OrderWorkflowServiceImpl;
import java.util.Optional;
import java.util.UUID;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.http.HttpStatus;

@ExtendWith(MockitoExtension.class)
class OrderWorkflowServiceImplTest {

  @Mock private OrdersRepository ordersRepository;
  @InjectMocks private OrderWorkflowServiceImpl service;

  // ── assertTransition ────────────────────────────────────────────────────

  @Test
  void assertTransition_validMove_noException() {
    // Should not throw
    service.assertTransition(OrderWorkflowStatus.CREATED, OrderWorkflowStatus.REDIRECTED);
  }

  @Test
  void assertTransition_illegalMove_throwsConflict() {
    assertThatThrownBy(
            () ->
                service.assertTransition(
                    OrderWorkflowStatus.CREATED, OrderWorkflowStatus.COMPLETED))
        .isInstanceOf(ApiException.class)
        .satisfies(ex -> assertThat(((ApiException) ex).getStatus()).isEqualTo(HttpStatus.CONFLICT))
        .satisfies(
            ex -> assertThat(((ApiException) ex).getCode()).isEqualTo("ILLEGAL_ORDER_TRANSITION"));
  }

  // ── transition ──────────────────────────────────────────────────────────

  @Test
  void transition_frozenOrder_throwsConflict() {
    UUID orderId = UUID.randomUUID();
    UUID actorId = UUID.randomUUID();
    OrdersEntity order = new OrdersEntity();
    order.setId(orderId);
    order.setIsDeleted(false);
    order.setFrozen(true);
    order.setWorkflowStatus(OrderWorkflowStatus.ORDERED);

    when(ordersRepository.findById(orderId)).thenReturn(Optional.of(order));

    assertThatThrownBy(
            () ->
                service.transition(
                    orderId,
                    OrderWorkflowStatus.ORDERED,
                    OrderWorkflowStatus.PROOF_SUBMITTED,
                    actorId))
        .isInstanceOf(ApiException.class)
        .satisfies(ex -> assertThat(((ApiException) ex).getCode()).isEqualTo("ORDER_FROZEN"));
  }

  @Test
  void transition_stateMismatch_throwsConflict() {
    UUID orderId = UUID.randomUUID();
    UUID actorId = UUID.randomUUID();
    OrdersEntity order = new OrdersEntity();
    order.setId(orderId);
    order.setIsDeleted(false);
    order.setFrozen(false);
    order.setWorkflowStatus(OrderWorkflowStatus.REDIRECTED); // actual state differs from 'from'

    when(ordersRepository.findById(orderId)).thenReturn(Optional.of(order));

    assertThatThrownBy(
            () ->
                service.transition(
                    orderId,
                    OrderWorkflowStatus.ORDERED, // caller thinks order is ORDERED
                    OrderWorkflowStatus.PROOF_SUBMITTED,
                    actorId))
        .isInstanceOf(ApiException.class)
        .satisfies(
            ex -> assertThat(((ApiException) ex).getCode()).isEqualTo("ORDER_STATE_MISMATCH"));
  }

  @Test
  void transition_reproofLimitExceeded_throwsTooManyRequests() {
    UUID orderId = UUID.randomUUID();
    UUID actorId = UUID.randomUUID();
    OrdersEntity order = new OrdersEntity();
    order.setId(orderId);
    order.setIsDeleted(false);
    order.setFrozen(false);
    order.setWorkflowStatus(OrderWorkflowStatus.REJECTED);
    // 5 rejections already recorded
    order.setEvents(
        "[{\"to\":\"REJECTED\"},{\"to\":\"REJECTED\"},{\"to\":\"REJECTED\"},{\"to\":\"REJECTED\"},{\"to\":\"REJECTED\"}]");

    when(ordersRepository.findById(orderId)).thenReturn(Optional.of(order));

    assertThatThrownBy(
            () ->
                service.transition(
                    orderId,
                    OrderWorkflowStatus.REJECTED,
                    OrderWorkflowStatus.PROOF_SUBMITTED,
                    actorId))
        .isInstanceOf(ApiException.class)
        .satisfies(
            ex ->
                assertThat(((ApiException) ex).getStatus()).isEqualTo(HttpStatus.TOO_MANY_REQUESTS))
        .satisfies(
            ex -> assertThat(((ApiException) ex).getCode()).isEqualTo("REPROOF_LIMIT_EXCEEDED"));
  }

  @Test
  void transition_concurrentConflict_throwsConflict() {
    UUID orderId = UUID.randomUUID();
    UUID actorId = UUID.randomUUID();
    OrdersEntity order = new OrdersEntity();
    order.setId(orderId);
    order.setIsDeleted(false);
    order.setFrozen(false);
    order.setWorkflowStatus(OrderWorkflowStatus.ORDERED);

    when(ordersRepository.findById(orderId)).thenReturn(Optional.of(order));
    when(ordersRepository.transitionWorkflowStatus(
            eq(orderId),
            eq(OrderWorkflowStatus.ORDERED),
            eq(OrderWorkflowStatus.PROOF_SUBMITTED),
            any()))
        .thenReturn(0); // concurrent update won the race

    assertThatThrownBy(
            () ->
                service.transition(
                    orderId,
                    OrderWorkflowStatus.ORDERED,
                    OrderWorkflowStatus.PROOF_SUBMITTED,
                    actorId))
        .isInstanceOf(ApiException.class)
        .satisfies(
            ex -> assertThat(((ApiException) ex).getCode()).isEqualTo("ORDER_STATE_MISMATCH"));
  }

  @Test
  void transition_success_returnsUpdatedOrder() {
    UUID orderId = UUID.randomUUID();
    UUID actorId = UUID.randomUUID();
    OrdersEntity order = new OrdersEntity();
    order.setId(orderId);
    order.setIsDeleted(false);
    order.setFrozen(false);
    order.setWorkflowStatus(OrderWorkflowStatus.ORDERED);
    OrdersEntity updated = new OrdersEntity();
    updated.setWorkflowStatus(OrderWorkflowStatus.PROOF_SUBMITTED);

    when(ordersRepository.findById(orderId))
        .thenReturn(Optional.of(order))
        .thenReturn(Optional.of(updated));
    when(ordersRepository.transitionWorkflowStatus(
            eq(orderId),
            eq(OrderWorkflowStatus.ORDERED),
            eq(OrderWorkflowStatus.PROOF_SUBMITTED),
            any()))
        .thenReturn(1);

    OrdersEntity result =
        service.transition(
            orderId, OrderWorkflowStatus.ORDERED, OrderWorkflowStatus.PROOF_SUBMITTED, actorId);

    assertThat(result.getWorkflowStatus()).isEqualTo(OrderWorkflowStatus.PROOF_SUBMITTED);
  }
}
