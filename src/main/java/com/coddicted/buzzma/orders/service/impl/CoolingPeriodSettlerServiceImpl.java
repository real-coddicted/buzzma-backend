package com.coddicted.buzzma.orders.service.impl;

import com.coddicted.buzzma.catalog.api.CatalogQueryPort;
import com.coddicted.buzzma.catalog.api.DealsResponseDto;
import com.coddicted.buzzma.identity.api.UserQueryPort;
import com.coddicted.buzzma.identity.api.UsersResponseDto;
import com.coddicted.buzzma.orders.api.OrderWorkflowService;
import com.coddicted.buzzma.orders.persistence.OrderItemsEntity;
import com.coddicted.buzzma.orders.persistence.OrderItemsRepository;
import com.coddicted.buzzma.orders.persistence.OrdersEntity;
import com.coddicted.buzzma.orders.persistence.OrdersRepository;
import com.coddicted.buzzma.orders.service.CoolingPeriodSettlerService;
import com.coddicted.buzzma.shared.enums.AffiliateStatus;
import com.coddicted.buzzma.shared.enums.OrderWorkflowStatus;
import com.coddicted.buzzma.shared.enums.PaymentStatus;
import com.coddicted.buzzma.shared.enums.TicketStatus;
import com.coddicted.buzzma.shared.enums.TransactionType;
import com.coddicted.buzzma.support.api.SupportQueryPort;
import com.coddicted.buzzma.wallet.api.WalletBusinessService;
import com.coddicted.buzzma.wallet.api.WalletMutationRequest;
import java.time.Instant;
import java.util.ArrayList;
import java.util.List;
import java.util.Optional;
import java.util.UUID;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Service;

@Service
public class CoolingPeriodSettlerServiceImpl implements CoolingPeriodSettlerService {

  private static final Logger LOG = LoggerFactory.getLogger(CoolingPeriodSettlerServiceImpl.class);

  private final OrdersRepository ordersRepository;
  private final OrderItemsRepository orderItemsRepository;
  private final CatalogQueryPort catalogQueryPort;
  private final SupportQueryPort supportQueryPort;
  private final UserQueryPort userQueryPort;
  private final WalletBusinessService walletBusinessService;
  private final OrderWorkflowService orderWorkflowService;

  public CoolingPeriodSettlerServiceImpl(
      OrdersRepository ordersRepository,
      OrderItemsRepository orderItemsRepository,
      CatalogQueryPort catalogQueryPort,
      SupportQueryPort supportQueryPort,
      UserQueryPort userQueryPort,
      WalletBusinessService walletBusinessService,
      OrderWorkflowService orderWorkflowService) {
    this.ordersRepository = ordersRepository;
    this.orderItemsRepository = orderItemsRepository;
    this.catalogQueryPort = catalogQueryPort;
    this.supportQueryPort = supportQueryPort;
    this.userQueryPort = userQueryPort;
    this.walletBusinessService = walletBusinessService;
    this.orderWorkflowService = orderWorkflowService;
  }

  @Override
  @Scheduled(fixedRateString = "${app.cooling-settler.interval-ms:3600000}")
  public List<OrdersEntity> runSettlement() {
    LOG.info("[cooling-settler] Starting cooling period settlement run");
    List<OrdersEntity> settled = new ArrayList<>();

    List<OrdersEntity> candidates =
        ordersRepository.findSettleable(
            OrderWorkflowStatus.APPROVED, AffiliateStatus.Pending_Cooling, Instant.now());

    for (OrdersEntity order : candidates) {
      try {
        boolean didSettle = settleOne(order);
        if (didSettle) {
          settled.add(order);
        }
      } catch (Exception e) {
        LOG.error(
            "[cooling-settler] Failed to settle order {}: {}", order.getId(), e.getMessage(), e);
      }
    }

    LOG.info(
        "[cooling-settler] Settlement run complete: settled={}, total={}",
        settled.size(),
        candidates.size());
    return settled;
  }

  private boolean settleOne(OrdersEntity order) {
    if (Boolean.TRUE.equals(order.getFrozen())) {
      LOG.info("[cooling-settler] Skipping frozen order {}", order.getId());
      return false;
    }
    if (order.getWorkflowStatus() != OrderWorkflowStatus.APPROVED) {
      return false;
    }

    boolean hasOpenDispute =
        supportQueryPort.existsOpenTicketForOrder(order.getId().toString(), TicketStatus.Open);
    if (hasOpenDispute) {
      order.setAffiliateStatus(AffiliateStatus.Frozen_Disputed);
      ordersRepository.save(order);
      LOG.info("[cooling-settler] Order {} has open dispute", order.getId());
      return false;
    }

    List<OrderItemsEntity> items =
        orderItemsRepository.findAllByOrderIdAndIsDeletedFalse(order.getId());
    if (items.isEmpty()) {
      LOG.warn("[cooling-settler] Order {} has no items, skipping", order.getId());
      return false;
    }

    OrderItemsEntity firstItem = items.get(0);
    UUID campaignId = firstItem.getCampaignId();
    String productId = firstItem.getProductId();
    int buyerCommissionPaise =
        firstItem.getCommissionPaise() != null ? firstItem.getCommissionPaise() : 0;

    if (productId == null || productId.isBlank()) {
      LOG.warn("[cooling-settler] Order {} has no productId, skipping", order.getId());
      return false;
    }

    Optional<DealsResponseDto> dealOpt;
    try {
      dealOpt = catalogQueryPort.findDealById(UUID.fromString(productId));
    } catch (IllegalArgumentException e) {
      LOG.warn("[cooling-settler] Invalid productId {} for order {}", productId, order.getId());
      return false;
    }

    if (dealOpt.isEmpty()) {
      LOG.warn("[cooling-settler] Deal {} not found for order {}", productId, order.getId());
      return false;
    }

    int payoutPaise = dealOpt.get().getPayoutPaise() != null ? dealOpt.get().getPayoutPaise() : 0;
    if (payoutPaise <= 0) {
      LOG.warn("[cooling-settler] Invalid payoutPaise {} for order {}", payoutPaise, order.getId());
      return false;
    }

    UUID buyerUserId = order.getUserId();
    UUID brandUserId = order.getBrandUserId();
    String mediatorCode = order.getManagerName();

    if (buyerUserId == null || brandUserId == null) {
      LOG.warn("[cooling-settler] Missing buyer/brand for order {}", order.getId());
      return false;
    }

    walletBusinessService.ensureWallet(brandUserId);
    walletBusinessService.ensureWallet(buyerUserId);

    walletBusinessService.debit(
        WalletMutationRequest.builder()
            .idempotencyKey("order-settlement-debit-" + order.getId())
            .type(TransactionType.order_settlement_debit)
            .ownerUserId(brandUserId)
            .fromUserId(brandUserId)
            .toUserId(buyerUserId)
            .amountPaise(payoutPaise)
            .orderId(order.getId().toString())
            .campaignId(campaignId)
            .metadata("{\"reason\":\"ORDER_PAYOUT\",\"source\":\"cooling-settler\"}")
            .build());

    if (buyerCommissionPaise > 0) {
      walletBusinessService.credit(
          WalletMutationRequest.builder()
              .idempotencyKey("order-commission-" + order.getId())
              .type(TransactionType.commission_settle)
              .ownerUserId(buyerUserId)
              .amountPaise(buyerCommissionPaise)
              .orderId(order.getId().toString())
              .campaignId(campaignId)
              .metadata("{\"reason\":\"ORDER_COMMISSION\",\"source\":\"cooling-settler\"}")
              .build());
    }

    int mediatorMarginPaise = payoutPaise - buyerCommissionPaise;
    if (mediatorMarginPaise > 0 && mediatorCode != null && !mediatorCode.isBlank()) {
      Optional<UsersResponseDto> mediator = userQueryPort.findByMediatorCode(mediatorCode);
      mediator.ifPresent(
          m -> {
            walletBusinessService.ensureWallet(m.getId());
            walletBusinessService.credit(
                WalletMutationRequest.builder()
                    .idempotencyKey("order-margin-" + order.getId())
                    .type(TransactionType.commission_settle)
                    .ownerUserId(m.getId())
                    .amountPaise(mediatorMarginPaise)
                    .orderId(order.getId().toString())
                    .campaignId(campaignId)
                    .metadata("{\"reason\":\"ORDER_MARGIN\",\"source\":\"cooling-settler\"}")
                    .build());
          });
    }

    order.setPaymentStatus(PaymentStatus.Paid);
    order.setAffiliateStatus(AffiliateStatus.Approved_Settled);
    ordersRepository.save(order);

    orderWorkflowService.transition(
        order.getId(), OrderWorkflowStatus.APPROVED, OrderWorkflowStatus.REWARD_PENDING, null);
    orderWorkflowService.transition(
        order.getId(), OrderWorkflowStatus.REWARD_PENDING, OrderWorkflowStatus.COMPLETED, null);

    LOG.info("[cooling-settler] Order {} auto-settled", order.getId());
    return true;
  }
}
