package com.mobo.service.impl;

import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.node.ObjectNode;
import com.mobo.common.AuditLogWriter;
import com.mobo.common.OffsetBasedPageRequest;
import com.mobo.dto.CampaignsResponseDto;
import com.mobo.dto.DealsResponseDto;
import com.mobo.dto.OrdersResponseDto;
import com.mobo.dto.PayoutsResponseDto;
import com.mobo.dto.UsersResponseDto;
import com.mobo.dto.WalletMutationRequest;
import com.mobo.entity.CampaignsEntity;
import com.mobo.entity.DealsEntity;
import com.mobo.entity.OrderItemsEntity;
import com.mobo.entity.OrdersEntity;
import com.mobo.entity.PayoutsEntity;
import com.mobo.entity.PendingConnectionsEntity;
import com.mobo.entity.UsersEntity;
import com.mobo.entity.enums.AffiliateStatus;
import com.mobo.entity.enums.CampaignStatus;
import com.mobo.entity.enums.KycStatus;
import com.mobo.entity.enums.OrderWorkflowStatus;
import com.mobo.entity.enums.PaymentStatus;
import com.mobo.entity.enums.PayoutStatus;
import com.mobo.entity.enums.RejectionType;
import com.mobo.entity.enums.TicketStatus;
import com.mobo.entity.enums.TransactionType;
import com.mobo.entity.enums.UserStatus;
import com.mobo.exception.ApiException;
import com.mobo.mapper.CampaignsMapper;
import com.mobo.mapper.DealsMapper;
import com.mobo.mapper.OrdersMapper;
import com.mobo.mapper.PayoutsMapper;
import com.mobo.mapper.UsersMapper;
import com.mobo.repository.CampaignsRepository;
import com.mobo.repository.DealsRepository;
import com.mobo.repository.OrderItemsRepository;
import com.mobo.repository.OrdersRepository;
import com.mobo.repository.PayoutsRepository;
import com.mobo.repository.PendingConnectionsRepository;
import com.mobo.repository.TicketsRepository;
import com.mobo.repository.UsersRepository;
import com.mobo.repository.WalletsRepository;
import com.mobo.service.OpsService;
import com.mobo.service.OrderWorkflowService;
import com.mobo.service.WalletBusinessService;
import java.time.Instant;
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
public class OpsServiceImpl implements OpsService {

  private final CampaignsRepository campaignsRepository;
  private final DealsRepository dealsRepository;
  private final OrdersRepository ordersRepository;
  private final OrderItemsRepository orderItemsRepository;
  private final UsersRepository usersRepository;
  private final WalletsRepository walletsRepository;
  private final PayoutsRepository payoutsRepository;
  private final PendingConnectionsRepository pendingConnectionsRepository;
  private final TicketsRepository ticketsRepository;
  private final OrderWorkflowService orderWorkflowService;
  private final WalletBusinessService walletBusinessService;
  private final AuditLogWriter auditLogWriter;
  private final CampaignsMapper campaignsMapper;
  private final DealsMapper dealsMapper;
  private final OrdersMapper ordersMapper;
  private final UsersMapper usersMapper;
  private final PayoutsMapper payoutsMapper;
  private final ObjectMapper objectMapper;

  public OpsServiceImpl(
      CampaignsRepository campaignsRepository,
      DealsRepository dealsRepository,
      OrdersRepository ordersRepository,
      OrderItemsRepository orderItemsRepository,
      UsersRepository usersRepository,
      WalletsRepository walletsRepository,
      PayoutsRepository payoutsRepository,
      PendingConnectionsRepository pendingConnectionsRepository,
      TicketsRepository ticketsRepository,
      OrderWorkflowService orderWorkflowService,
      WalletBusinessService walletBusinessService,
      AuditLogWriter auditLogWriter,
      CampaignsMapper campaignsMapper,
      DealsMapper dealsMapper,
      OrdersMapper ordersMapper,
      UsersMapper usersMapper,
      PayoutsMapper payoutsMapper,
      ObjectMapper objectMapper) {
    this.campaignsRepository = campaignsRepository;
    this.dealsRepository = dealsRepository;
    this.ordersRepository = ordersRepository;
    this.orderItemsRepository = orderItemsRepository;
    this.usersRepository = usersRepository;
    this.walletsRepository = walletsRepository;
    this.payoutsRepository = payoutsRepository;
    this.pendingConnectionsRepository = pendingConnectionsRepository;
    this.ticketsRepository = ticketsRepository;
    this.orderWorkflowService = orderWorkflowService;
    this.walletBusinessService = walletBusinessService;
    this.auditLogWriter = auditLogWriter;
    this.campaignsMapper = campaignsMapper;
    this.dealsMapper = dealsMapper;
    this.ordersMapper = ordersMapper;
    this.usersMapper = usersMapper;
    this.payoutsMapper = payoutsMapper;
    this.objectMapper = objectMapper;
  }

  // ── Campaign CRUD ─────────────────────────────────────────────────────────────

  @Override
  @Transactional
  public CampaignsResponseDto createCampaign(
      UUID brandUserId,
      String title,
      String platform,
      String image,
      String productUrl,
      int originalPricePaise,
      int pricePaise,
      int payoutPaise,
      int totalSlots,
      String dealType,
      int returnWindowDays,
      String[] allowedAgencyCodes,
      UUID actorUserId) {

    CampaignsEntity campaign = new CampaignsEntity();
    campaign.setBrandUserId(brandUserId);
    campaign.setTitle(title);
    campaign.setPlatform(platform);
    campaign.setImage(image);
    campaign.setProductUrl(productUrl);
    campaign.setOriginalPricePaise(originalPricePaise);
    campaign.setPricePaise(pricePaise);
    campaign.setPayoutPaise(payoutPaise);
    campaign.setTotalSlots(totalSlots);
    campaign.setUsedSlots(0);
    campaign.setStatus(CampaignStatus.active);
    campaign.setAllowedAgencyCodes(allowedAgencyCodes != null ? allowedAgencyCodes : new String[0]);
    campaign.setReturnWindowDays(returnWindowDays > 0 ? returnWindowDays : 14);
    if (dealType != null) {
      try {
        campaign.setDealType(com.mobo.entity.enums.DealType.valueOf(dealType));
      } catch (IllegalArgumentException ignored) {
        // default kept
      }
    }
    campaign.setCreatedBy(actorUserId);
    campaign.setUpdatedBy(actorUserId);

    // Populate brandName from user
    usersRepository
        .findById(brandUserId)
        .ifPresent(u -> campaign.setBrandName(u.getName() != null ? u.getName() : "Brand"));

    CampaignsEntity saved = campaignsRepository.save(campaign);
    auditLogWriter.write(
        actorUserId,
        new String[] {"ops"},
        "CAMPAIGN_CREATED",
        "Campaign",
        saved.getId().toString(),
        null);
    return campaignsMapper.toResponse(saved);
  }

  @Override
  @Transactional
  public CampaignsResponseDto copyCampaign(UUID campaignId, UUID actorUserId) {
    CampaignsEntity source =
        campaignsRepository
            .findById(campaignId)
            .filter(c -> !Boolean.TRUE.equals(c.getIsDeleted()))
            .orElseThrow(() -> new ApiException(HttpStatus.NOT_FOUND, "CAMPAIGN_NOT_FOUND"));

    CampaignsEntity copy = new CampaignsEntity();
    copy.setBrandUserId(source.getBrandUserId());
    copy.setBrandName(source.getBrandName());
    copy.setTitle(source.getTitle() + " (copy)");
    copy.setPlatform(source.getPlatform());
    copy.setImage(source.getImage());
    copy.setProductUrl(source.getProductUrl());
    copy.setOriginalPricePaise(source.getOriginalPricePaise());
    copy.setPricePaise(source.getPricePaise());
    copy.setPayoutPaise(source.getPayoutPaise());
    copy.setTotalSlots(source.getTotalSlots());
    copy.setUsedSlots(0);
    copy.setStatus(CampaignStatus.draft);
    copy.setDealType(source.getDealType());
    copy.setReturnWindowDays(source.getReturnWindowDays());
    copy.setAllowedAgencyCodes(
        source.getAllowedAgencyCodes() != null ? source.getAllowedAgencyCodes() : new String[0]);
    copy.setCreatedBy(actorUserId);
    copy.setUpdatedBy(actorUserId);

    CampaignsEntity saved = campaignsRepository.save(copy);
    auditLogWriter.write(
        actorUserId,
        new String[] {"ops"},
        "CAMPAIGN_COPIED",
        "Campaign",
        saved.getId().toString(),
        "{\"sourceCampaignId\":\"" + campaignId + "\"}");
    return campaignsMapper.toResponse(saved);
  }

  @Override
  @Transactional
  public CampaignsResponseDto updateCampaignStatus(
      UUID campaignId, String status, UUID actorUserId) {
    CampaignsEntity campaign =
        campaignsRepository
            .findById(campaignId)
            .filter(c -> !Boolean.TRUE.equals(c.getIsDeleted()))
            .orElseThrow(() -> new ApiException(HttpStatus.NOT_FOUND, "CAMPAIGN_NOT_FOUND"));

    CampaignStatus newStatus;
    try {
      newStatus = CampaignStatus.valueOf(status.toLowerCase());
    } catch (IllegalArgumentException e) {
      throw new ApiException(HttpStatus.BAD_REQUEST, "INVALID_STATUS");
    }

    campaign.setStatus(newStatus);
    campaign.setUpdatedBy(actorUserId);
    CampaignsEntity saved = campaignsRepository.save(campaign);

    auditLogWriter.write(
        actorUserId,
        new String[] {"ops"},
        "CAMPAIGN_STATUS_CHANGED",
        "Campaign",
        campaignId.toString(),
        "{\"status\":\"" + status + "\"}");
    return campaignsMapper.toResponse(saved);
  }

  @Override
  @Transactional
  public void deleteCampaign(UUID campaignId, UUID actorUserId) {
    CampaignsEntity campaign =
        campaignsRepository
            .findById(campaignId)
            .filter(c -> !Boolean.TRUE.equals(c.getIsDeleted()))
            .orElseThrow(() -> new ApiException(HttpStatus.NOT_FOUND, "CAMPAIGN_NOT_FOUND"));

    // Block deletion if campaign has orders
    List<OrderItemsEntity> items =
        orderItemsRepository.findAllByCampaignIdAndIsDeletedFalse(campaignId);
    if (!items.isEmpty()) {
      throw new ApiException(HttpStatus.CONFLICT, "CAMPAIGN_HAS_ORDERS");
    }

    campaign.setIsDeleted(true);
    campaign.setUpdatedBy(actorUserId);
    campaignsRepository.save(campaign);

    // Deactivate associated deals
    dealsRepository
        .findAllByMediatorCodeAndIsDeletedFalseAndActiveTrue("")
        .forEach(
            d -> {
              // no-op placeholder — deal soft-delete handled separately via deal service
            });

    auditLogWriter.write(
        actorUserId,
        new String[] {"ops"},
        "CAMPAIGN_DELETED",
        "Campaign",
        campaignId.toString(),
        null);
  }

  @Override
  @Transactional
  public void assignSlots(UUID campaignId, Map<String, Object> assignments, UUID actorUserId) {
    CampaignsEntity campaign =
        campaignsRepository
            .findById(campaignId)
            .filter(c -> !Boolean.TRUE.equals(c.getIsDeleted()))
            .orElseThrow(() -> new ApiException(HttpStatus.NOT_FOUND, "CAMPAIGN_NOT_FOUND"));

    CampaignStatus status = campaign.getStatus();
    if (status != CampaignStatus.active && status != CampaignStatus.draft) {
      throw new ApiException(HttpStatus.CONFLICT, "CAMPAIGN_NOT_ACTIVE");
    }

    // Parse and validate total assigned slots
    int totalAssigned = 0;
    ObjectNode current;
    try {
      String existingJson = campaign.getAssignments() != null ? campaign.getAssignments() : "{}";
      current = (ObjectNode) objectMapper.readTree(existingJson);
    } catch (Exception e) {
      current = objectMapper.createObjectNode();
    }

    for (Map.Entry<String, Object> entry : assignments.entrySet()) {
      String code = entry.getKey().toLowerCase();
      Object val = entry.getValue();
      int limit = 0;
      if (val instanceof Number) {
        limit = ((Number) val).intValue();
      } else if (val instanceof Map) {
        Object l = ((Map<?, ?>) val).get("limit");
        if (l instanceof Number) {
          limit = ((Number) l).intValue();
        }
      }
      if (limit > 0) {
        ObjectNode slotNode = objectMapper.createObjectNode();
        slotNode.put("limit", limit);
        slotNode.put("payout", campaign.getPayoutPaise());
        current.set(code, slotNode);
        totalAssigned += limit;
      }
    }

    if (totalAssigned > campaign.getTotalSlots()) {
      throw new ApiException(HttpStatus.CONFLICT, "ASSIGNMENT_EXCEEDS_TOTAL_SLOTS");
    }

    try {
      campaign.setAssignments(objectMapper.writeValueAsString(current));
    } catch (Exception e) {
      campaign.setAssignments("{}");
    }

    if (campaign.getStatus() == CampaignStatus.draft) {
      campaign.setStatus(CampaignStatus.active);
    }
    if (!Boolean.TRUE.equals(campaign.getLocked())) {
      campaign.setLocked(true);
      campaign.setLockedAt(Instant.now());
      campaign.setLockedReason("SLOT_ASSIGNMENT");
    }
    campaign.setUpdatedBy(actorUserId);
    campaignsRepository.save(campaign);

    auditLogWriter.write(
        actorUserId,
        new String[] {"ops"},
        "CAMPAIGN_SLOTS_ASSIGNED",
        "Campaign",
        campaignId.toString(),
        null);
  }

  // ── Deal management ──────────────────────────────────────────────────────────

  @Override
  @Transactional
  public DealsResponseDto publishDeal(UUID campaignId, String mediatorCode, UUID actorUserId) {
    CampaignsEntity campaign =
        campaignsRepository
            .findById(campaignId)
            .filter(c -> !Boolean.TRUE.equals(c.getIsDeleted()))
            .orElseThrow(() -> new ApiException(HttpStatus.NOT_FOUND, "CAMPAIGN_NOT_FOUND"));

    if (campaign.getStatus() != CampaignStatus.active) {
      throw new ApiException(HttpStatus.CONFLICT, "CAMPAIGN_NOT_ACTIVE");
    }

    // Idempotent: return existing deal if already published
    java.util.Optional<DealsEntity> existing =
        dealsRepository.findByCampaignIdAndMediatorCode(campaignId, mediatorCode);
    if (existing.isPresent() && !Boolean.TRUE.equals(existing.get().getIsDeleted())) {
      return dealsMapper.toResponse(existing.get());
    }

    DealsEntity deal = new DealsEntity();
    deal.setCampaignId(campaignId);
    deal.setMediatorCode(mediatorCode);
    deal.setTitle(campaign.getTitle());
    deal.setImage(campaign.getImage());
    deal.setProductUrl(campaign.getProductUrl());
    deal.setPlatform(campaign.getPlatform());
    deal.setBrandName(campaign.getBrandName());
    deal.setDealType(campaign.getDealType());
    deal.setOriginalPricePaise(campaign.getOriginalPricePaise());
    deal.setPricePaise(campaign.getPricePaise());
    deal.setPayoutPaise(campaign.getPayoutPaise());
    deal.setCommissionPaise(campaign.getPayoutPaise());
    deal.setActive(true);
    deal.setCreatedBy(actorUserId);
    deal.setUpdatedBy(actorUserId);

    DealsEntity saved = dealsRepository.save(deal);
    auditLogWriter.write(
        actorUserId,
        new String[] {"ops"},
        "DEAL_PUBLISHED",
        "Deal",
        saved.getId().toString(),
        "{\"campaignId\":\"" + campaignId + "\",\"mediatorCode\":\"" + mediatorCode + "\"}");
    return dealsMapper.toResponse(saved);
  }

  @Override
  @Transactional
  public void declineOffer(UUID dealId, UUID actorUserId) {
    DealsEntity deal =
        dealsRepository
            .findById(dealId)
            .filter(d -> !Boolean.TRUE.equals(d.getIsDeleted()))
            .orElseThrow(() -> new ApiException(HttpStatus.NOT_FOUND, "DEAL_NOT_FOUND"));

    deal.setActive(false);
    deal.setIsDeleted(true);
    deal.setUpdatedBy(actorUserId);
    dealsRepository.save(deal);

    auditLogWriter.write(
        actorUserId, new String[] {"ops"}, "DEAL_DECLINED", "Deal", dealId.toString(), null);
  }

  // ── Order verification ────────────────────────────────────────────────────────

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

    auditLogWriter.write(
        actorUserId,
        new String[] {"ops"},
        "ORDER_VERIFIED",
        "Order",
        orderId.toString(),
        "{\"step\":\"order\"}");
    return ordersMapper.toResponse(saved);
  }

  @Override
  @Transactional
  public OrdersResponseDto verifyOrderRequirement(UUID orderId, String type, UUID actorUserId) {
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

    auditLogWriter.write(
        actorUserId,
        new String[] {"ops"},
        "ORDER_VERIFIED",
        "Order",
        orderId.toString(),
        "{\"step\":\"" + type + "\"}");
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

    auditLogWriter.write(
        actorUserId,
        new String[] {"ops"},
        "ORDER_ALL_STEPS_VERIFIED",
        "Order",
        orderId.toString(),
        null);
    return ordersMapper.toResponse(saved);
  }

  @Override
  @Transactional
  public void rejectOrderProof(UUID orderId, String type, String reason, UUID actorUserId) {
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

    auditLogWriter.write(
        actorUserId,
        new String[] {"ops"},
        "ORDER_PROOF_REJECTED",
        "Order",
        orderId.toString(),
        "{\"type\":\"" + type + "\"}");
  }

  @Override
  @Transactional
  public void cancelOrderProofs(UUID orderId, String reason, UUID actorUserId) {
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

    auditLogWriter.write(
        actorUserId,
        new String[] {"ops"},
        "PROOFS_CANCELLED",
        "Order",
        orderId.toString(),
        "{\"reason\":\"" + (reason != null ? escapeJson(reason) : "") + "\"}");
  }

  @Override
  @Transactional
  public void requestMissingProof(UUID orderId, String type, String note, UUID actorUserId) {
    OrdersEntity order = loadActiveOrder(orderId);

    if (!isValidRequirementType(type) && !"order".equals(type)) {
      throw new ApiException(HttpStatus.BAD_REQUEST, "INVALID_PROOF_TYPE");
    }

    if (hasProof(order, type)) {
      return; // already satisfied
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

    auditLogWriter.write(
        actorUserId,
        new String[] {"ops"},
        "MISSING_PROOF_REQUESTED",
        "Order",
        orderId.toString(),
        "{\"type\":\"" + type + "\"}");
  }

  @Override
  @Transactional
  public void forceApproveOrder(UUID orderId, UUID actorUserId) {
    OrdersEntity order = loadActiveOrder(orderId);

    if (Boolean.TRUE.equals(order.getFrozen())) {
      throw new ApiException(HttpStatus.CONFLICT, "ORDER_FROZEN");
    }

    OrderWorkflowStatus wf = order.getWorkflowStatus();
    if (wf != OrderWorkflowStatus.UNDER_REVIEW && wf != OrderWorkflowStatus.PROOF_SUBMITTED) {
      throw new ApiException(HttpStatus.CONFLICT, "INVALID_WORKFLOW_STATE");
    }

    // Force all verifications as complete
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

    // If PROOF_SUBMITTED, first move to UNDER_REVIEW
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

    auditLogWriter.write(
        actorUserId,
        new String[] {"ops"},
        "ORDER_FORCE_APPROVED",
        "Order",
        orderId.toString(),
        null);
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

    auditLogWriter.write(
        actorUserId,
        new String[] {"ops"},
        "ORDER_CANCELLED",
        "Order",
        orderId.toString(),
        "{\"reason\":\"" + (reason != null ? escapeJson(reason) : "") + "\"}");
  }

  // ── Settlement ────────────────────────────────────────────────────────────────

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

    // Check open dispute ticket
    boolean hasDispute =
        ticketsRepository.existsByOrderIdAndStatusAndIsDeletedFalse(
            orderId.toString(), TicketStatus.Open);
    if (hasDispute) {
      order.setAffiliateStatus(AffiliateStatus.Frozen_Disputed);
      ordersRepository.save(order);
      throw new ApiException(HttpStatus.CONFLICT, "FROZEN_DISPUTE");
    }

    // Check buyer is active
    UsersEntity buyer =
        usersRepository
            .findById(order.getUserId())
            .orElseThrow(() -> new ApiException(HttpStatus.NOT_FOUND, "BUYER_NOT_FOUND"));
    if (buyer.getStatus() != UserStatus.active || Boolean.TRUE.equals(buyer.getIsDeleted())) {
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

      DealsEntity deal =
          dealsRepository
              .findById(UUID.fromString(productId))
              .filter(d -> !Boolean.TRUE.equals(d.getIsDeleted()))
              .orElseThrow(() -> new ApiException(HttpStatus.CONFLICT, "DEAL_NOT_FOUND"));

      int payoutPaise = deal.getPayoutPaise();
      if (payoutPaise <= 0) {
        throw new ApiException(HttpStatus.CONFLICT, "INVALID_PAYOUT");
      }

      UUID brandUserId = order.getBrandUserId();
      if (brandUserId == null) {
        throw new ApiException(HttpStatus.CONFLICT, "MISSING_BRAND");
      }

      walletBusinessService.ensureWallet(brandUserId);
      walletBusinessService.ensureWallet(order.getUserId());

      // Debit brand
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

      // Credit buyer (commission)
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

      // Credit mediator (margin)
      int mediatorMargin = payoutPaise - commissionPaise;
      String mediatorCode = order.getManagerName();
      if (mediatorMargin > 0 && mediatorCode != null) {
        usersRepository
            .findByMediatorCodeAndIsDeletedFalse(mediatorCode)
            .ifPresent(
                mediator -> {
                  walletBusinessService.ensureWallet(mediator.getId());
                  walletBusinessService.credit(
                      WalletMutationRequest.builder()
                          .idempotencyKey("settle-margin-" + orderId)
                          .type(TransactionType.commission_settle)
                          .ownerUserId(mediator.getId())
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

    // Workflow: APPROVED -> REWARD_PENDING -> COMPLETED
    orderWorkflowService.transition(
        orderId, OrderWorkflowStatus.APPROVED, OrderWorkflowStatus.REWARD_PENDING, actorUserId);
    orderWorkflowService.transition(
        orderId, OrderWorkflowStatus.REWARD_PENDING, OrderWorkflowStatus.COMPLETED, actorUserId);

    auditLogWriter.write(
        actorUserId,
        new String[] {"ops"},
        "ORDER_SETTLED",
        "Order",
        orderId.toString(),
        "{\"settlementMode\":\"" + (settlementMode != null ? settlementMode : "wallet") + "\"}");
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

      DealsEntity deal =
          dealsRepository
              .findById(UUID.fromString(productId))
              .filter(d -> !Boolean.TRUE.equals(d.getIsDeleted()))
              .orElseThrow(() -> new ApiException(HttpStatus.CONFLICT, "DEAL_NOT_FOUND"));

      int payoutPaise = deal.getPayoutPaise();
      UUID brandUserId = order.getBrandUserId();
      if (brandUserId == null) {
        throw new ApiException(HttpStatus.CONFLICT, "MISSING_BRAND");
      }

      walletBusinessService.ensureWallet(brandUserId);

      // Credit brand back
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

      // Debit buyer
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

      // Debit mediator
      int mediatorMargin = payoutPaise - commissionPaise;
      String mediatorCode = order.getManagerName();
      if (mediatorMargin > 0 && mediatorCode != null) {
        usersRepository
            .findByMediatorCodeAndIsDeletedFalse(mediatorCode)
            .ifPresent(
                mediator ->
                    walletBusinessService.debit(
                        WalletMutationRequest.builder()
                            .idempotencyKey("unsettle-debit-mediator-" + orderId)
                            .type(TransactionType.margin_reversal)
                            .ownerUserId(mediator.getId())
                            .fromUserId(mediator.getId())
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

    auditLogWriter.write(
        actorUserId, new String[] {"ops"}, "ORDER_UNSETTLED", "Order", orderId.toString(), null);
  }

  // ── Mediator management ──────────────────────────────────────────────────────

  @Override
  @Transactional
  public void approveMediator(UUID mediatorUserId, UUID actorUserId) {
    UsersEntity mediator =
        usersRepository
            .findById(mediatorUserId)
            .filter(u -> !Boolean.TRUE.equals(u.getIsDeleted()))
            .orElseThrow(() -> new ApiException(HttpStatus.NOT_FOUND, "USER_NOT_FOUND"));

    mediator.setKycStatus(KycStatus.verified);
    mediator.setStatus(UserStatus.active);
    mediator.setUpdatedBy(actorUserId);
    usersRepository.save(mediator);

    auditLogWriter.write(
        actorUserId,
        new String[] {"ops"},
        "MEDIATOR_APPROVED",
        "User",
        mediatorUserId.toString(),
        null);
  }

  @Override
  @Transactional
  public void rejectMediator(UUID mediatorUserId, UUID actorUserId) {
    UsersEntity mediator =
        usersRepository
            .findById(mediatorUserId)
            .filter(u -> !Boolean.TRUE.equals(u.getIsDeleted()))
            .orElseThrow(() -> new ApiException(HttpStatus.NOT_FOUND, "USER_NOT_FOUND"));

    mediator.setKycStatus(KycStatus.rejected);
    mediator.setStatus(UserStatus.suspended);
    mediator.setUpdatedBy(actorUserId);
    usersRepository.save(mediator);

    auditLogWriter.write(
        actorUserId,
        new String[] {"ops"},
        "MEDIATOR_REJECTED",
        "User",
        mediatorUserId.toString(),
        null);
  }

  @Override
  @Transactional
  public void approveUser(UUID userId, UUID actorUserId) {
    UsersEntity user =
        usersRepository
            .findById(userId)
            .filter(u -> !Boolean.TRUE.equals(u.getIsDeleted()))
            .orElseThrow(() -> new ApiException(HttpStatus.NOT_FOUND, "USER_NOT_FOUND"));

    user.setIsVerifiedByMediator(true);
    user.setUpdatedBy(actorUserId);
    usersRepository.save(user);

    auditLogWriter.write(
        actorUserId, new String[] {"ops"}, "BUYER_APPROVED", "User", userId.toString(), null);
  }

  @Override
  @Transactional
  public void rejectUser(UUID userId, UUID actorUserId) {
    UsersEntity user =
        usersRepository
            .findById(userId)
            .filter(u -> !Boolean.TRUE.equals(u.getIsDeleted()))
            .orElseThrow(() -> new ApiException(HttpStatus.NOT_FOUND, "USER_NOT_FOUND"));

    user.setStatus(UserStatus.suspended);
    user.setUpdatedBy(actorUserId);
    usersRepository.save(user);

    auditLogWriter.write(
        actorUserId, new String[] {"ops"}, "USER_REJECTED", "User", userId.toString(), null);
  }

  @Override
  @Transactional
  public PayoutsResponseDto payoutMediator(UUID mediatorUserId, int amountPaise, UUID actorUserId) {
    UsersEntity mediator =
        usersRepository
            .findById(mediatorUserId)
            .filter(u -> !Boolean.TRUE.equals(u.getIsDeleted()))
            .orElseThrow(() -> new ApiException(HttpStatus.NOT_FOUND, "USER_NOT_FOUND"));

    com.mobo.entity.WalletsEntity wallet =
        walletsRepository
            .findByOwnerUserId(mediatorUserId)
            .orElseThrow(() -> new ApiException(HttpStatus.NOT_FOUND, "WALLET_NOT_FOUND"));

    PayoutsEntity payout = new PayoutsEntity();
    payout.setBeneficiaryUserId(mediatorUserId);
    payout.setWalletId(wallet.getId());
    payout.setAmountPaise(amountPaise);
    payout.setCurrency("INR");
    payout.setStatus(PayoutStatus.recorded);
    payout.setRequestedAt(Instant.now());
    payout.setCreatedBy(actorUserId);
    payout.setUpdatedBy(actorUserId);

    PayoutsEntity saved = payoutsRepository.save(payout);
    auditLogWriter.write(
        actorUserId,
        new String[] {"ops"},
        "MEDIATOR_PAYOUT",
        "Payout",
        saved.getId().toString(),
        "{\"mediatorUserId\":\"" + mediatorUserId + "\",\"amountPaise\":" + amountPaise + "}");
    return payoutsMapper.toResponse(saved);
  }

  @Override
  @Transactional
  public void deletePayout(UUID payoutId, UUID actorUserId) {
    PayoutsEntity payout =
        payoutsRepository
            .findById(payoutId)
            .filter(p -> !Boolean.TRUE.equals(p.getIsDeleted()))
            .orElseThrow(() -> new ApiException(HttpStatus.NOT_FOUND, "PAYOUT_NOT_FOUND"));

    if (payout.getStatus() == PayoutStatus.paid || payout.getStatus() == PayoutStatus.processing) {
      throw new ApiException(HttpStatus.CONFLICT, "PAYOUT_NOT_DELETABLE");
    }

    payout.setIsDeleted(true);
    payout.setUpdatedBy(actorUserId);
    payoutsRepository.save(payout);

    auditLogWriter.write(
        actorUserId, new String[] {"ops"}, "PAYOUT_DELETED", "Payout", payoutId.toString(), null);
  }

  // ── Dashboard / listing ──────────────────────────────────────────────────────

  @Override
  @Transactional(readOnly = true)
  public Map<String, Object> getDashboardStats() {
    Map<String, Object> stats = new HashMap<>();
    stats.put("totalOrders", ordersRepository.count());
    stats.put("activeCampaigns", campaignsRepository.count());
    stats.put("totalUsers", usersRepository.count());
    return stats;
  }

  @Override
  @Transactional(readOnly = true)
  public List<UsersResponseDto> getMediators(String agencyCode, int limit, int offset) {
    var pageable =
        new OffsetBasedPageRequest(limit, offset, Sort.by(Sort.Direction.DESC, "createdAt"));
    return usersRepository.findAllByParentCodeAndIsDeletedFalse(agencyCode).stream()
        .map(usersMapper::toResponse)
        .collect(Collectors.toList());
  }

  @Override
  @Transactional(readOnly = true)
  public List<CampaignsResponseDto> getCampaigns(String mediatorCode, int limit, int offset) {
    var pageable =
        new OffsetBasedPageRequest(limit, offset, Sort.by(Sort.Direction.DESC, "createdAt"));
    return campaignsRepository.findAllByIsDeletedFalse(pageable).stream()
        .map(campaignsMapper::toResponse)
        .collect(Collectors.toList());
  }

  @Override
  @Transactional(readOnly = true)
  public List<DealsResponseDto> getDeals(List<String> mediatorCodes, int limit, int offset) {
    if (mediatorCodes == null || mediatorCodes.isEmpty()) {
      return List.of();
    }
    var pageable =
        new OffsetBasedPageRequest(limit, offset, Sort.by(Sort.Direction.DESC, "createdAt"));
    return dealsRepository
        .findAllByMediatorCodeInAndIsDeletedFalse(mediatorCodes, pageable)
        .stream()
        .map(dealsMapper::toResponse)
        .collect(Collectors.toList());
  }

  @Override
  @Transactional(readOnly = true)
  public List<OrdersResponseDto> getOrders(List<String> managerCodes, int limit, int offset) {
    if (managerCodes == null || managerCodes.isEmpty()) {
      return List.of();
    }
    var pageable =
        new OffsetBasedPageRequest(limit, offset, Sort.by(Sort.Direction.DESC, "createdAt"));
    return ordersRepository.findAllByManagerNameInAndIsDeletedFalse(managerCodes, pageable).stream()
        .map(ordersMapper::toResponse)
        .collect(Collectors.toList());
  }

  @Override
  @Transactional(readOnly = true)
  public List<UsersResponseDto> getPendingUsers(String parentCode, int limit, int offset) {
    var pageable =
        new OffsetBasedPageRequest(limit, offset, Sort.by(Sort.Direction.DESC, "createdAt"));
    return usersRepository
        .findAllByParentCodeAndIsVerifiedByMediatorAndIsDeletedFalse(parentCode, false, pageable)
        .stream()
        .map(usersMapper::toResponse)
        .collect(Collectors.toList());
  }

  @Override
  @Transactional(readOnly = true)
  public List<UsersResponseDto> getVerifiedUsers(String parentCode, int limit, int offset) {
    var pageable =
        new OffsetBasedPageRequest(limit, offset, Sort.by(Sort.Direction.DESC, "createdAt"));
    return usersRepository
        .findAllByParentCodeAndIsVerifiedByMediatorAndIsDeletedFalse(parentCode, true, pageable)
        .stream()
        .map(usersMapper::toResponse)
        .collect(Collectors.toList());
  }

  @Override
  @Transactional(readOnly = true)
  public List<PayoutsResponseDto> getLedger(int limit, int offset) {
    var pageable =
        new OffsetBasedPageRequest(limit, offset, Sort.by(Sort.Direction.DESC, "requestedAt"));
    return payoutsRepository.findAllByIsDeletedFalse(pageable).stream()
        .map(payoutsMapper::toResponse)
        .collect(Collectors.toList());
  }

  // ── Connections ───────────────────────────────────────────────────────────────

  @Override
  @Transactional
  public void requestBrandConnection(
      String brandCode, String agencyCode, String agencyName, UUID actorUserId) {
    UsersEntity brand =
        usersRepository
            .findByMediatorCodeAndIsDeletedFalse(brandCode)
            .orElseThrow(() -> new ApiException(HttpStatus.NOT_FOUND, "BRAND_NOT_FOUND"));

    if (brand.getStatus() != UserStatus.active) {
      throw new ApiException(HttpStatus.CONFLICT, "BRAND_SUSPENDED");
    }

    // Check already connected
    String[] connected =
        brand.getConnectedAgencies() != null ? brand.getConnectedAgencies() : new String[0];
    for (String c : connected) {
      if (c.equals(agencyCode)) {
        throw new ApiException(HttpStatus.CONFLICT, "ALREADY_CONNECTED");
      }
    }

    // Check duplicate pending request
    pendingConnectionsRepository
        .findByUserIdAndAgencyCodeAndIsDeletedFalse(brand.getId(), agencyCode)
        .ifPresent(
            p -> {
              throw new ApiException(HttpStatus.CONFLICT, "ALREADY_REQUESTED");
            });

    // Check pending count limit
    int pendingCount = pendingConnectionsRepository.countByUserIdAndIsDeletedFalse(brand.getId());
    if (pendingCount >= 100) {
      throw new ApiException(HttpStatus.CONFLICT, "TOO_MANY_PENDING");
    }

    PendingConnectionsEntity conn = new PendingConnectionsEntity();
    conn.setUserId(brand.getId());
    conn.setAgencyId(actorUserId.toString());
    conn.setAgencyName(agencyName);
    conn.setAgencyCode(agencyCode);
    conn.setTimestamp(Instant.now());
    pendingConnectionsRepository.save(conn);

    auditLogWriter.write(
        actorUserId,
        new String[] {"agency"},
        "BRAND_CONNECTION_REQUESTED",
        "User",
        brand.getId().toString(),
        "{\"agencyCode\":\"" + agencyCode + "\",\"brandCode\":\"" + brandCode + "\"}");
  }

  // ── Private helpers ───────────────────────────────────────────────────────────

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

    // All steps verified — transition to APPROVED
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
