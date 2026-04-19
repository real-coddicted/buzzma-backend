package com.coddicted.buzzma.orders.service.impl;

import com.coddicted.buzzma.catalog.api.CatalogQueryPort;
import com.coddicted.buzzma.catalog.api.DealsResponseDto;
import com.coddicted.buzzma.identity.api.UserQueryPort;
import com.coddicted.buzzma.identity.api.UsersResponseDto;
import com.coddicted.buzzma.orders.api.OrderAdminPort;
import com.coddicted.buzzma.orders.api.OrderWorkflowService;
import com.coddicted.buzzma.orders.api.OrdersResponseDto;
import com.coddicted.buzzma.orders.mapper.OrdersMapper;
import com.coddicted.buzzma.orders.persistence.OrderItemsEntity;
import com.coddicted.buzzma.orders.persistence.OrderItemsRepository;
import com.coddicted.buzzma.orders.persistence.OrdersEntity;
import com.coddicted.buzzma.orders.persistence.OrdersRepository;
import com.coddicted.buzzma.shared.enums.AffiliateStatus;
import com.coddicted.buzzma.shared.enums.OrderWorkflowStatus;
import com.coddicted.buzzma.shared.enums.PaymentStatus;
import com.coddicted.buzzma.shared.enums.RejectionType;
import com.coddicted.buzzma.shared.enums.TicketStatus;
import com.coddicted.buzzma.shared.enums.TransactionType;
import com.coddicted.buzzma.shared.enums.UserStatus;
import com.coddicted.buzzma.shared.exception.ApiException;
import com.coddicted.buzzma.support.api.SupportQueryPort;
import com.coddicted.buzzma.wallet.api.WalletBusinessService;
import com.coddicted.buzzma.wallet.api.WalletMutationRequest;
import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.node.ObjectNode;
import java.time.Instant;
import java.util.List;
import java.util.Optional;
import java.util.UUID;
import java.util.stream.Collectors;
import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
public class OrderAdminAdapter implements OrderAdminPort {

  private final OrdersRepository ordersRepository;
  private final OrderItemsRepository orderItemsRepository;
  private final OrdersMapper ordersMapper;
  private final OrderWorkflowService orderWorkflowService;
  private final SupportQueryPort supportQueryPort;
  private final UserQueryPort userQueryPort;
  private final CatalogQueryPort catalogQueryPort;
  private final WalletBusinessService walletBusinessService;
  private final ObjectMapper objectMapper;

  public OrderAdminAdapter(
      OrdersRepository ordersRepository,
      OrderItemsRepository orderItemsRepository,
      OrdersMapper ordersMapper,
      OrderWorkflowService orderWorkflowService,
      SupportQueryPort supportQueryPort,
      UserQueryPort userQueryPort,
      CatalogQueryPort catalogQueryPort,
      WalletBusinessService walletBusinessService,
      ObjectMapper objectMapper) {
    this.ordersRepository = ordersRepository;
    this.orderItemsRepository = orderItemsRepository;
    this.ordersMapper = ordersMapper;
    this.orderWorkflowService = orderWorkflowService;
    this.supportQueryPort = supportQueryPort;
    this.userQueryPort = userQueryPort;
    this.catalogQueryPort = catalogQueryPort;
    this.walletBusinessService = walletBusinessService;
    this.objectMapper = objectMapper;
  }

  @Override
  @Transactional
  public OrdersResponseDto verifyOrderClaim(UUID orderId, UUID actorUserId) {
    OrdersEntity order = loadActiveOrder(orderId);

    if (Boolean.TRUE.equals(order.getFrozen())) {
      throw new ApiException(HttpStatus.CONFLICT, "ORDER_FROZEN");
    }
    if (order.getWorkflowStatus() != OrderWorkflowStatus.UNDER_REVIEW) {
      throw new ApiException(HttpStatus.CONFLICT, "INVALID_WORKFLOW_STATE");
    }

    ObjectNode v = parseVerification(order.getVerification());
    if (v.has("order") && v.get("order").has("verifiedAt")) {
      return ordersMapper.toResponse(order);
    }

    ObjectNode orderNode = objectMapper.createObjectNode();
    orderNode.put("verifiedAt", Instant.now().toString());
    orderNode.put("verifiedBy", actorUserId.toString());
    v.set("order", orderNode);

    order.setVerification(writeJson(v));
    order.setUpdatedBy(actorUserId);
    OrdersEntity saved = ordersRepository.save(order);

    saved = finalizeApprovalIfReady(saved, actorUserId);
    return ordersMapper.toResponse(saved);
  }

  @Override
  @Transactional
  public OrdersResponseDto verifyRequirement(UUID orderId, String type, UUID actorUserId) {
    OrdersEntity order = loadActiveOrder(orderId);

    if (Boolean.TRUE.equals(order.getFrozen())) {
      throw new ApiException(HttpStatus.CONFLICT, "ORDER_FROZEN");
    }
    if (order.getWorkflowStatus() != OrderWorkflowStatus.UNDER_REVIEW) {
      throw new ApiException(HttpStatus.CONFLICT, "INVALID_WORKFLOW_STATE");
    }

    ObjectNode v = parseVerification(order.getVerification());
    if (!v.has("order") || !v.get("order").has("verifiedAt")) {
      throw new ApiException(HttpStatus.CONFLICT, "PURCHASE_NOT_VERIFIED");
    }

    if (!isValidRequirementType(type)) {
      throw new ApiException(HttpStatus.BAD_REQUEST, "INVALID_REQUIREMENT_TYPE");
    }
    if (!hasProof(order, type)) {
      throw new ApiException(HttpStatus.CONFLICT, "MISSING_PROOF");
    }
    if (v.has(type) && v.get(type).has("verifiedAt")) {
      return ordersMapper.toResponse(order);
    }

    ObjectNode typeNode = objectMapper.createObjectNode();
    typeNode.put("verifiedAt", Instant.now().toString());
    typeNode.put("verifiedBy", actorUserId.toString());
    v.set(type, typeNode);

    order.setVerification(writeJson(v));
    order.setUpdatedBy(actorUserId);
    OrdersEntity saved = ordersRepository.save(order);

    saved = finalizeApprovalIfReady(saved, actorUserId);
    return ordersMapper.toResponse(saved);
  }

  @Override
  @Transactional
  public OrdersResponseDto verifyAllSteps(UUID orderId, UUID actorUserId) {
    OrdersEntity order = loadActiveOrder(orderId);

    if (Boolean.TRUE.equals(order.getFrozen())) {
      throw new ApiException(HttpStatus.CONFLICT, "ORDER_FROZEN");
    }
    if (order.getWorkflowStatus() != OrderWorkflowStatus.UNDER_REVIEW) {
      throw new ApiException(HttpStatus.CONFLICT, "INVALID_WORKFLOW_STATE");
    }

    List<OrderItemsEntity> items = orderItemsRepository.findAllByOrderIdAndIsDeletedFalse(orderId);
    List<String> required = getRequiredSteps(items);
    List<String> missingProofs =
        required.stream().filter(t -> !hasProof(order, t)).collect(Collectors.toList());
    if (!missingProofs.isEmpty()) {
      throw new ApiException(HttpStatus.CONFLICT, "MISSING_PROOFS");
    }

    ObjectNode v = parseVerification(order.getVerification());
    String now = Instant.now().toString();

    if (!v.has("order") || !v.get("order").has("verifiedAt")) {
      ObjectNode orderNode = objectMapper.createObjectNode();
      orderNode.put("verifiedAt", now);
      orderNode.put("verifiedBy", actorUserId.toString());
      v.set("order", orderNode);
    }
    for (String type : required) {
      if (!v.has(type) || !v.get(type).has("verifiedAt")) {
        ObjectNode typeNode = objectMapper.createObjectNode();
        typeNode.put("verifiedAt", now);
        typeNode.put("verifiedBy", actorUserId.toString());
        v.set(type, typeNode);
      }
    }

    order.setVerification(writeJson(v));
    order.setUpdatedBy(actorUserId);
    OrdersEntity saved = ordersRepository.save(order);

    saved = finalizeApprovalIfReady(saved, actorUserId);
    return ordersMapper.toResponse(saved);
  }

  @Override
  @Transactional
  public void rejectProof(UUID orderId, String type, String reason, UUID actorUserId) {
    OrdersEntity order = loadActiveOrder(orderId);

    if (Boolean.TRUE.equals(order.getFrozen())) {
      throw new ApiException(HttpStatus.CONFLICT, "ORDER_FROZEN");
    }

    OrderWorkflowStatus wf = order.getWorkflowStatus();
    if (wf != OrderWorkflowStatus.UNDER_REVIEW && wf != OrderWorkflowStatus.APPROVED) {
      throw new ApiException(HttpStatus.CONFLICT, "INVALID_WORKFLOW_STATE");
    }

    boolean wasApproved = (wf == OrderWorkflowStatus.APPROVED);
    ObjectNode v = parseVerification(order.getVerification());

    if ("order".equals(type)) {
      if (order.getScreenshotOrder() == null) {
        throw new ApiException(HttpStatus.CONFLICT, "MISSING_PROOF");
      }
      order.setScreenshotOrder(null);
      v.remove("order");
    } else {
      if (!v.has("order") || !v.get("order").has("verifiedAt")) {
        throw new ApiException(HttpStatus.CONFLICT, "PURCHASE_NOT_VERIFIED");
      }
      if (!hasProof(order, type)) {
        throw new ApiException(HttpStatus.CONFLICT, "MISSING_PROOF");
      }
      if ("review".equals(type)) {
        order.setScreenshotReview(null);
        order.setReviewLink(null);
        v.remove("review");
      } else if ("rating".equals(type)) {
        order.setScreenshotRating(null);
        order.setRatingAiVerification(null);
        v.remove("rating");
      } else if ("returnWindow".equals(type)) {
        order.setScreenshotReturnWindow(null);
        v.remove("returnWindow");
      }
    }

    try {
      order.setRejectionType(RejectionType.valueOf(type));
    } catch (IllegalArgumentException ignored) {
      // leave null
    }
    order.setRejectionReason(reason);
    order.setRejectionAt(Instant.now());
    order.setRejectionBy(actorUserId);
    order.setAffiliateStatus(wasApproved ? AffiliateStatus.Unchecked : AffiliateStatus.Rejected);
    if (wasApproved) {
      order.setExpectedSettlementDate(null);
    }
    order.setVerification(writeJson(v));
    order.setUpdatedBy(actorUserId);
    ordersRepository.save(order);

    if (wasApproved) {
      orderWorkflowService.transition(
          orderId, OrderWorkflowStatus.APPROVED, OrderWorkflowStatus.ORDERED, actorUserId);
    }
  }

  @Override
  @Transactional
  public void cancelAllProofs(UUID orderId, String reason, UUID actorUserId) {
    OrdersEntity order = loadActiveOrder(orderId);

    if (Boolean.TRUE.equals(order.getFrozen())) {
      throw new ApiException(HttpStatus.CONFLICT, "ORDER_FROZEN");
    }

    OrderWorkflowStatus wf = order.getWorkflowStatus();
    if (wf != OrderWorkflowStatus.UNDER_REVIEW
        && wf != OrderWorkflowStatus.PROOF_SUBMITTED
        && wf != OrderWorkflowStatus.APPROVED) {
      throw new ApiException(HttpStatus.CONFLICT, "INVALID_WORKFLOW_STATE");
    }

    order.setScreenshotOrder(null);
    order.setScreenshotReview(null);
    order.setScreenshotRating(null);
    order.setScreenshotReturnWindow(null);
    order.setReviewLink(null);
    order.setVerification("{}");
    order.setRatingAiVerification(null);
    order.setReturnWindowAiVerification(null);
    order.setOrderAiVerification(null);
    order.setReviewerName(null);
    order.setAffiliateStatus(AffiliateStatus.Unchecked);
    order.setRejectionType(null);
    order.setRejectionReason(null);
    order.setRejectionAt(null);
    order.setRejectionBy(null);
    order.setMissingProofRequests("[]");
    if (wf == OrderWorkflowStatus.APPROVED) {
      order.setExpectedSettlementDate(null);
    }
    order.setUpdatedBy(actorUserId);
    ordersRepository.save(order);

    orderWorkflowService.transition(orderId, wf, OrderWorkflowStatus.ORDERED, actorUserId);
  }

  @Override
  @Transactional
  public void requestMissingProof(UUID orderId, String type, String note, UUID actorUserId) {
    OrdersEntity order = loadActiveOrder(orderId);

    if (!isValidRequirementType(type) && !"order".equals(type)) {
      throw new ApiException(HttpStatus.BAD_REQUEST, "INVALID_PROOF_TYPE");
    }

    if (hasProof(order, type)) {
      return;
    }

    String existingRequests =
        order.getMissingProofRequests() != null ? order.getMissingProofRequests() : "[]";
    String newEntry =
        String.format(
            "{\"type\":\"%s\",\"note\":\"%s\",\"requestedAt\":\"%s\",\"requestedBy\":\"%s\"}",
            type, note != null ? escapeJson(note) : "", Instant.now(), actorUserId);
    String updated = appendToJsonArray(existingRequests, newEntry);
    order.setMissingProofRequests(updated);
    order.setUpdatedBy(actorUserId);
    ordersRepository.save(order);
  }

  @Override
  @Transactional
  public void forceApprove(UUID orderId, UUID actorUserId) {
    OrdersEntity order = loadActiveOrder(orderId);

    if (Boolean.TRUE.equals(order.getFrozen())) {
      throw new ApiException(HttpStatus.CONFLICT, "ORDER_FROZEN");
    }

    OrderWorkflowStatus wf = order.getWorkflowStatus();
    if (wf != OrderWorkflowStatus.UNDER_REVIEW && wf != OrderWorkflowStatus.PROOF_SUBMITTED) {
      throw new ApiException(HttpStatus.CONFLICT, "INVALID_WORKFLOW_STATE");
    }

    ObjectNode v = parseVerification(order.getVerification());
    String now = Instant.now().toString();
    ObjectNode orderNode = objectMapper.createObjectNode();
    orderNode.put("verifiedAt", now);
    orderNode.put("verifiedBy", actorUserId.toString());
    orderNode.put("forced", true);
    v.set("order", orderNode);

    List<OrderItemsEntity> items = orderItemsRepository.findAllByOrderIdAndIsDeletedFalse(orderId);
    for (String type : getRequiredSteps(items)) {
      ObjectNode typeNode = objectMapper.createObjectNode();
      typeNode.put("verifiedAt", now);
      typeNode.put("verifiedBy", actorUserId.toString());
      typeNode.put("forced", true);
      v.set(type, typeNode);
    }
    order.setVerification(writeJson(v));

    if (wf == OrderWorkflowStatus.PROOF_SUBMITTED) {
      orderWorkflowService.transition(
          orderId,
          OrderWorkflowStatus.PROOF_SUBMITTED,
          OrderWorkflowStatus.UNDER_REVIEW,
          actorUserId);
    }

    order = loadActiveOrder(orderId);
    order.setVerification(writeJson(v));
    order.setUpdatedBy(actorUserId);
    ordersRepository.save(order);

    orderWorkflowService.transition(
        orderId, OrderWorkflowStatus.UNDER_REVIEW, OrderWorkflowStatus.APPROVED, actorUserId);
  }

  @Override
  @Transactional
  public void cancelOrder(UUID orderId, String reason, UUID actorUserId) {
    OrdersEntity order = loadActiveOrder(orderId);

    OrderWorkflowStatus wf = order.getWorkflowStatus();
    if (wf == OrderWorkflowStatus.COMPLETED || wf == OrderWorkflowStatus.FAILED) {
      throw new ApiException(HttpStatus.CONFLICT, "ORDER_ALREADY_TERMINAL");
    }

    orderWorkflowService.transition(orderId, wf, OrderWorkflowStatus.FAILED, actorUserId);
  }

  @Override
  @Transactional
  public void settleOrderPayment(
      UUID orderId, String settlementRef, String settlementMode, UUID actorUserId) {
    OrdersEntity order = loadActiveOrder(orderId);

    if (Boolean.TRUE.equals(order.getFrozen())) {
      throw new ApiException(HttpStatus.CONFLICT, "ORDER_FROZEN");
    }
    if (order.getWorkflowStatus() != OrderWorkflowStatus.APPROVED) {
      throw new ApiException(HttpStatus.CONFLICT, "INVALID_WORKFLOW_STATE");
    }

    boolean hasDispute =
        supportQueryPort.existsOpenTicketForOrder(orderId.toString(), TicketStatus.Open);
    if (hasDispute) {
      order.setAffiliateStatus(AffiliateStatus.Frozen_Disputed);
      ordersRepository.save(order);
      throw new ApiException(HttpStatus.CONFLICT, "FROZEN_DISPUTE");
    }

    Optional<UserStatus> buyerStatus = userQueryPort.findStatusById(order.getUserId());
    if (buyerStatus.isEmpty()) {
      throw new ApiException(HttpStatus.NOT_FOUND, "BUYER_NOT_FOUND");
    }
    if (buyerStatus.get() != UserStatus.active) {
      throw new ApiException(HttpStatus.CONFLICT, "FROZEN_SUSPENSION");
    }

    boolean useWallet = !"external".equals(settlementMode);

    if (useWallet) {
      List<OrderItemsEntity> items =
          orderItemsRepository.findAllByOrderIdAndIsDeletedFalse(orderId);
      if (items.isEmpty()) {
        throw new ApiException(HttpStatus.CONFLICT, "MISSING_DEAL_ID");
      }

      String productId = items.get(0).getProductId();
      UUID campaignId = items.get(0).getCampaignId();
      int commissionPaise = items.get(0).getCommissionPaise();

      DealsResponseDto deal =
          catalogQueryPort
              .findDealById(UUID.fromString(productId))
              .orElseThrow(() -> new ApiException(HttpStatus.CONFLICT, "DEAL_NOT_FOUND"));

      int payoutPaise = deal.getPayoutPaise() != null ? deal.getPayoutPaise() : 0;
      if (payoutPaise <= 0) {
        throw new ApiException(HttpStatus.CONFLICT, "INVALID_PAYOUT");
      }

      UUID brandUserId = order.getBrandUserId();
      if (brandUserId == null) {
        throw new ApiException(HttpStatus.CONFLICT, "MISSING_BRAND");
      }

      walletBusinessService.ensureWallet(brandUserId);
      walletBusinessService.ensureWallet(order.getUserId());

      walletBusinessService.debit(
          WalletMutationRequest.builder()
              .idempotencyKey("settle-debit-" + orderId)
              .type(TransactionType.order_settlement_debit)
              .ownerUserId(brandUserId)
              .fromUserId(brandUserId)
              .toUserId(order.getUserId())
              .amountPaise(payoutPaise)
              .orderId(orderId.toString())
              .campaignId(campaignId)
              .metadata("{\"reason\":\"ORDER_PAYOUT\"}")
              .build());

      if (commissionPaise > 0) {
        walletBusinessService.credit(
            WalletMutationRequest.builder()
                .idempotencyKey("settle-commission-" + orderId)
                .type(TransactionType.commission_settle)
                .ownerUserId(order.getUserId())
                .amountPaise(commissionPaise)
                .orderId(orderId.toString())
                .campaignId(campaignId)
                .metadata("{\"reason\":\"ORDER_COMMISSION\"}")
                .build());
      }

      int mediatorMargin = payoutPaise - commissionPaise;
      String mediatorCode = order.getManagerName();
      if (mediatorMargin > 0 && mediatorCode != null) {
        Optional<UsersResponseDto> mediator = userQueryPort.findByMediatorCode(mediatorCode);
        mediator.ifPresent(
            m -> {
              walletBusinessService.ensureWallet(m.getId());
              walletBusinessService.credit(
                  WalletMutationRequest.builder()
                      .idempotencyKey("settle-margin-" + orderId)
                      .type(TransactionType.commission_settle)
                      .ownerUserId(m.getId())
                      .amountPaise(mediatorMargin)
                      .orderId(orderId.toString())
                      .campaignId(campaignId)
                      .metadata("{\"reason\":\"ORDER_MARGIN\"}")
                      .build());
            });
      }
    }

    order.setPaymentStatus(PaymentStatus.Paid);
    order.setAffiliateStatus(AffiliateStatus.Approved_Settled);
    if (settlementRef != null && !settlementRef.isBlank()) {
      order.setSettlementRef(settlementRef);
    }
    order.setUpdatedBy(actorUserId);
    ordersRepository.save(order);

    orderWorkflowService.transition(
        orderId, OrderWorkflowStatus.APPROVED, OrderWorkflowStatus.REWARD_PENDING, actorUserId);
    orderWorkflowService.transition(
        orderId, OrderWorkflowStatus.REWARD_PENDING, OrderWorkflowStatus.COMPLETED, actorUserId);
  }

  @Override
  @Transactional
  public void unsettleOrderPayment(UUID orderId, UUID actorUserId) {
    OrdersEntity order = loadActiveOrder(orderId);

    if (Boolean.TRUE.equals(order.getFrozen())) {
      throw new ApiException(HttpStatus.CONFLICT, "ORDER_FROZEN");
    }

    OrderWorkflowStatus wf = order.getWorkflowStatus();
    if (wf != OrderWorkflowStatus.COMPLETED
        && wf != OrderWorkflowStatus.FAILED
        && wf != OrderWorkflowStatus.REWARD_PENDING) {
      throw new ApiException(HttpStatus.CONFLICT, "INVALID_WORKFLOW_STATE");
    }
    if (order.getPaymentStatus() != PaymentStatus.Paid) {
      throw new ApiException(HttpStatus.CONFLICT, "NOT_SETTLED");
    }

    List<OrderItemsEntity> items = orderItemsRepository.findAllByOrderIdAndIsDeletedFalse(orderId);
    boolean isCapExceeded = order.getAffiliateStatus() == AffiliateStatus.Cap_Exceeded;
    boolean isExternal =
        order.getSettlementMode() != null && order.getSettlementMode().name().equals("external");

    if (!isCapExceeded && !isExternal && !items.isEmpty()) {
      String productId = items.get(0).getProductId();
      UUID campaignId = items.get(0).getCampaignId();
      int commissionPaise = items.get(0).getCommissionPaise();

      DealsResponseDto deal =
          catalogQueryPort
              .findDealById(UUID.fromString(productId))
              .orElseThrow(() -> new ApiException(HttpStatus.CONFLICT, "DEAL_NOT_FOUND"));

      int payoutPaise = deal.getPayoutPaise() != null ? deal.getPayoutPaise() : 0;
      UUID brandUserId = order.getBrandUserId();
      if (brandUserId == null) {
        throw new ApiException(HttpStatus.CONFLICT, "MISSING_BRAND");
      }

      walletBusinessService.ensureWallet(brandUserId);

      walletBusinessService.credit(
          WalletMutationRequest.builder()
              .idempotencyKey("unsettle-credit-brand-" + orderId)
              .type(TransactionType.refund)
              .ownerUserId(brandUserId)
              .fromUserId(order.getUserId())
              .toUserId(brandUserId)
              .amountPaise(payoutPaise)
              .orderId(orderId.toString())
              .campaignId(campaignId)
              .metadata("{\"reason\":\"ORDER_UNSETTLE\"}")
              .build());

      if (commissionPaise > 0) {
        walletBusinessService.debit(
            WalletMutationRequest.builder()
                .idempotencyKey("unsettle-debit-buyer-" + orderId)
                .type(TransactionType.commission_reversal)
                .ownerUserId(order.getUserId())
                .fromUserId(order.getUserId())
                .toUserId(brandUserId)
                .amountPaise(commissionPaise)
                .orderId(orderId.toString())
                .campaignId(campaignId)
                .metadata("{\"reason\":\"ORDER_UNSETTLE_COMMISSION\"}")
                .build());
      }

      int mediatorMargin = payoutPaise - commissionPaise;
      String mediatorCode = order.getManagerName();
      if (mediatorMargin > 0 && mediatorCode != null) {
        Optional<UsersResponseDto> mediator = userQueryPort.findByMediatorCode(mediatorCode);
        mediator.ifPresent(
            m ->
                walletBusinessService.debit(
                    WalletMutationRequest.builder()
                        .idempotencyKey("unsettle-debit-mediator-" + orderId)
                        .type(TransactionType.margin_reversal)
                        .ownerUserId(m.getId())
                        .fromUserId(m.getId())
                        .toUserId(brandUserId)
                        .amountPaise(mediatorMargin)
                        .orderId(orderId.toString())
                        .campaignId(campaignId)
                        .metadata("{\"reason\":\"ORDER_UNSETTLE_MARGIN\"}")
                        .build()));
      }
    }

    order.setWorkflowStatus(OrderWorkflowStatus.APPROVED);
    order.setPaymentStatus(PaymentStatus.Pending);
    order.setAffiliateStatus(AffiliateStatus.Pending_Cooling);
    order.setSettlementRef(null);
    order.setUpdatedBy(actorUserId);
    ordersRepository.save(order);
  }

  @Override
  @Transactional(readOnly = true)
  public long countByCampaignId(UUID campaignId) {
    return orderItemsRepository.findAllByCampaignIdAndIsDeletedFalse(campaignId).size();
  }

  // ── Private helpers (mirrors OpsServiceImpl originals) ─────────────────────

  private OrdersEntity loadActiveOrder(UUID orderId) {
    return ordersRepository
        .findById(orderId)
        .filter(o -> !Boolean.TRUE.equals(o.getIsDeleted()))
        .orElseThrow(() -> new ApiException(HttpStatus.NOT_FOUND, "ORDER_NOT_FOUND"));
  }

  private ObjectNode parseVerification(String json) {
    if (json == null || json.isBlank()) {
      return objectMapper.createObjectNode();
    }
    try {
      JsonNode node = objectMapper.readTree(json);
      if (node instanceof ObjectNode) {
        return (ObjectNode) node;
      }
    } catch (Exception ignored) {
      // fall through
    }
    return objectMapper.createObjectNode();
  }

  private String writeJson(ObjectNode node) {
    try {
      return objectMapper.writeValueAsString(node);
    } catch (Exception e) {
      return "{}";
    }
  }

  private boolean hasProof(OrdersEntity order, String type) {
    return switch (type) {
      case "order" -> order.getScreenshotOrder() != null;
      case "review" -> order.getScreenshotReview() != null || order.getReviewLink() != null;
      case "rating" -> order.getScreenshotRating() != null;
      case "returnWindow" -> order.getScreenshotReturnWindow() != null;
      default -> false;
    };
  }

  private boolean isValidRequirementType(String type) {
    return "review".equals(type) || "rating".equals(type) || "returnWindow".equals(type);
  }

  private List<String> getRequiredSteps(List<OrderItemsEntity> items) {
    boolean requiresReview = items.stream().anyMatch(i -> "Review".equals(i.getDealType()));
    boolean requiresRating = items.stream().anyMatch(i -> "Rating".equals(i.getDealType()));
    java.util.ArrayList<String> required = new java.util.ArrayList<>();
    if (requiresReview) {
      required.add("review");
    }
    if (requiresRating) {
      required.add("rating");
    }
    required.add("returnWindow");
    return required;
  }

  private OrdersEntity finalizeApprovalIfReady(OrdersEntity order, UUID actorUserId) {
    if (order.getWorkflowStatus() != OrderWorkflowStatus.UNDER_REVIEW) {
      return order;
    }

    ObjectNode v = parseVerification(order.getVerification());
    if (!v.has("order") || !v.get("order").has("verifiedAt")) {
      return order;
    }

    List<OrderItemsEntity> items =
        orderItemsRepository.findAllByOrderIdAndIsDeletedFalse(order.getId());
    if (items.isEmpty()) {
      return order;
    }

    List<String> required = getRequiredSteps(items);
    boolean allProofsPresent = required.stream().allMatch(t -> hasProof(order, t));
    if (!allProofsPresent) {
      return order;
    }

    boolean allVerified = required.stream().allMatch(t -> v.has(t) && v.get(t).has("verifiedAt"));
    if (!allVerified) {
      return order;
    }

    return orderWorkflowService.transition(
        order.getId(), OrderWorkflowStatus.UNDER_REVIEW, OrderWorkflowStatus.APPROVED, actorUserId);
  }

  private String appendToJsonArray(String jsonArray, String element) {
    String trimmed = jsonArray.trim();
    if ("[]".equals(trimmed)) {
      return "[" + element + "]";
    }
    return trimmed.substring(0, trimmed.length() - 1) + "," + element + "]";
  }

  private String escapeJson(String s) {
    return s.replace("\\", "\\\\").replace("\"", "\\\"");
  }
}
