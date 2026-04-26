package com.coddicted.buzzma.ops.web;

import com.coddicted.buzzma.catalog.api.CampaignsResponseDto;
import com.coddicted.buzzma.catalog.api.DealsResponseDto;
import com.coddicted.buzzma.identity.api.UsersResponseDto;
import com.coddicted.buzzma.identity.persistence.InvitesEntity;
import com.coddicted.buzzma.identity.persistence.InvitesRepository;
import com.coddicted.buzzma.identity.persistence.UsersEntity;
import com.coddicted.buzzma.identity.persistence.UsersRepository;
import com.coddicted.buzzma.ops.service.OpsService;
import com.coddicted.buzzma.orders.api.OrdersResponseDto;
import com.coddicted.buzzma.shared.enums.UserRole;
import com.coddicted.buzzma.shared.exception.ApiException;
import com.coddicted.buzzma.shared.security.CurrentUserId;
import com.coddicted.buzzma.wallet.api.PayoutsResponseDto;
import jakarta.validation.Valid;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Positive;
import java.time.Instant;
import java.util.Arrays;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.UUID;
import org.springframework.http.HttpStatus;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestMethod;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.ResponseStatus;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/api/ops")
@PreAuthorize("isAuthenticated()")
public class OpsController {

  private final OpsService opsService;
  private final UsersRepository usersRepository;
  private final InvitesRepository invitesRepository;

  public OpsController(
      OpsService opsService, UsersRepository usersRepository, InvitesRepository invitesRepository) {
    this.opsService = opsService;
    this.usersRepository = usersRepository;
    this.invitesRepository = invitesRepository;
  }

  // ── Campaign CRUD ─────────────────────────────────────────────────────────────

  @PostMapping("/campaigns")
  @ResponseStatus(HttpStatus.CREATED)
  @PreAuthorize("hasAnyRole('ops','admin','agency')")
  public CampaignsResponseDto createCampaign(
      @Valid @RequestBody CreateCampaignRequest req, @CurrentUserId UUID actorUserId) {
    return opsService.createCampaign(
        req.brandUserId(),
        req.brandName(),
        req.title(),
        req.platform(),
        req.image(),
        req.productUrl(),
        req.resolvedOriginalPricePaise(),
        req.resolvedPricePaise(),
        req.resolvedPayoutPaise(),
        req.totalSlots(),
        req.dealType(),
        req.returnWindowDays() != null ? req.returnWindowDays() : 14,
        req.resolvedAllowedAgencyCodes(),
        actorUserId);
  }

  @PostMapping("/campaigns/{campaignId}/copy")
  @PreAuthorize("hasAnyRole('ops','admin')")
  public CampaignsResponseDto copyCampaignByPath(
      @PathVariable UUID campaignId, @CurrentUserId UUID actorUserId) {
    return opsService.copyCampaign(campaignId, actorUserId);
  }

  @PostMapping("/campaigns/copy")
  @PreAuthorize("isAuthenticated()")
  public Map<String, Object> copyCampaign(
      @RequestBody Map<String, Object> body, @CurrentUserId UUID actorUserId) {
    Object idVal = body.get("id");
    if (idVal == null) {
      throw new com.coddicted.buzzma.shared.exception.ApiException(
          org.springframework.http.HttpStatus.BAD_REQUEST, "MISSING_ID");
    }
    UUID campaignId = UUID.fromString(String.valueOf(idVal));
    CampaignsResponseDto c = opsService.copyCampaign(campaignId, actorUserId);

    Map<String, Object> campaign = new LinkedHashMap<>();
    campaign.put("id", c.getId());
    campaign.put("title", c.getTitle());
    campaign.put("image", c.getImage());
    campaign.put("dealType", c.getDealType());
    campaign.put("totalSlots", c.getTotalSlots() != null ? c.getTotalSlots() : 0);
    campaign.put("usedSlots", 0);
    campaign.put("status", "draft");
    campaign.put("price", c.getPricePaise() != null ? c.getPricePaise() / 100.0 : 0);
    campaign.put("payout", c.getPayoutPaise() != null ? c.getPayoutPaise() / 100.0 : 0);
    campaign.put("assignments", Map.of());
    campaign.put("brandId", c.getBrandUserId());

    Map<String, Object> result = new LinkedHashMap<>();
    result.put("ok", true);
    result.put("id", c.getId());
    result.put("campaign", campaign);
    return result;
  }

  @RequestMapping(
      value = "/campaigns/{campaignId}/status",
      method = {RequestMethod.POST, RequestMethod.PATCH})
  @PreAuthorize("hasAnyRole('ops','admin','agency')")
  public CampaignsResponseDto updateCampaignStatus(
      @PathVariable UUID campaignId,
      @RequestBody Map<String, String> body,
      @CurrentUserId UUID actorUserId) {
    return opsService.updateCampaignStatus(campaignId, body.get("status"), actorUserId);
  }

  @DeleteMapping("/campaigns/{campaignId}")
  @ResponseStatus(HttpStatus.NO_CONTENT)
  @PreAuthorize("hasAnyRole('ops','admin','agency')")
  public void deleteCampaign(@PathVariable UUID campaignId, @CurrentUserId UUID actorUserId) {
    opsService.deleteCampaign(campaignId, actorUserId);
  }

  @PostMapping("/campaigns/{campaignId}/slots")
  @PreAuthorize("hasAnyRole('ops','admin','agency')")
  public Map<String, Boolean> assignSlots(
      @PathVariable UUID campaignId,
      @RequestBody Map<String, Object> assignments,
      @CurrentUserId UUID actorUserId) {
    opsService.assignSlots(campaignId, assignments, actorUserId);
    return Map.of("ok", true);
  }

  @PostMapping("/campaigns/assign")
  @PreAuthorize("isAuthenticated()")
  public Map<String, Object> assignSlotsFlat(
      @RequestBody Map<String, Object> body, @CurrentUserId UUID actorUserId) {
    Object idVal = body.get("id");
    if (idVal == null) {
      throw new com.coddicted.buzzma.shared.exception.ApiException(
          org.springframework.http.HttpStatus.BAD_REQUEST, "MISSING_ID");
    }
    UUID campaignId = UUID.fromString(String.valueOf(idVal));

    @SuppressWarnings("unchecked")
    Map<String, Object> assignments =
        body.get("assignments") instanceof Map<?, ?> m ? (Map<String, Object>) m : Map.of();

    opsService.assignSlots(campaignId, assignments, actorUserId);
    return Map.of("ok", true);
  }

  @GetMapping("/campaigns")
  @PreAuthorize("hasAnyRole('ops','admin','agency','mediator')")
  public List<Map<String, Object>> getCampaigns(
      @RequestParam(required = false) String mediatorCode,
      @RequestParam(defaultValue = "50") int limit,
      @RequestParam(defaultValue = "0") int offset,
      @CurrentUserId UUID actorId) {
    String resolvedCode = resolveCode(mediatorCode, actorId);
    return opsService.getCampaigns(resolvedCode, limit, offset).stream()
        .map(c -> toUiCampaign(c, resolvedCode))
        .toList();
  }

  @SuppressWarnings("unchecked")
  private String resolveCode(String paramCode, UUID actorId) {
    if (paramCode != null && !paramCode.isBlank()) {
      return paramCode;
    }
    if (actorId == null) {
      return null;
    }
    return usersRepository
        .findById(actorId)
        .map(u -> u.getMediatorCode() != null ? u.getMediatorCode() : u.getParentCode())
        .orElse(null);
  }

  static Map<String, Object> toUiCampaign(CampaignsResponseDto c, String requesterMediatorCode) {
    Map<String, Object> statusMap =
        Map.of("active", "Active", "paused", "Paused", "completed", "Completed", "draft", "Draft");

    // Parse assignments JSONB string into a map
    Map<String, Object> assignmentsRaw = new java.util.LinkedHashMap<>();
    Map<String, Object> assignments = new java.util.LinkedHashMap<>();
    Map<String, Object> assignmentDetails = new java.util.LinkedHashMap<>();
    if (c.getAssignments() != null && !c.getAssignments().isBlank()) {
      try {
        assignmentsRaw =
            new com.fasterxml.jackson.databind.ObjectMapper()
                .readValue(c.getAssignments(), Map.class);
      } catch (Exception ignored) {
      }
    }
    int payoutPaise = c.getPayoutPaise() != null ? c.getPayoutPaise() : 0;
    for (Map.Entry<String, Object> entry : assignmentsRaw.entrySet()) {
      String code = entry.getKey().toLowerCase();
      Object raw = entry.getValue();
      if (raw instanceof Number n) {
        assignments.put(code, n.intValue());
        Map<String, Object> det = new java.util.LinkedHashMap<>();
        det.put("limit", n.intValue());
        det.put("payout", payoutPaise / 100.0);
        det.put("commission", 0);
        assignmentDetails.put(code, det);
      } else if (raw instanceof Map<?, ?> rawMap) {
        int limit2 = rawMap.get("limit") instanceof Number ln ? ln.intValue() : 0;
        int payout2 = rawMap.get("payout") instanceof Number pn ? pn.intValue() : payoutPaise;
        int commission2 = rawMap.get("commissionPaise") instanceof Number cn ? cn.intValue() : 0;
        assignments.put(code, limit2);
        Map<String, Object> det = new java.util.LinkedHashMap<>();
        det.put("limit", limit2);
        det.put("payout", payout2 / 100.0);
        det.put("commission", commission2 / 100.0);
        assignmentDetails.put(code, det);
      } else {
        assignments.put(code, 0);
        Map<String, Object> det = new java.util.LinkedHashMap<>();
        det.put("limit", 0);
        det.put("payout", payoutPaise / 100.0);
        det.put("commission", 0);
        assignmentDetails.put(code, det);
      }
    }

    Map<String, Object> m = new LinkedHashMap<>();
    m.put("id", c.getId());
    m.put("title", c.getTitle() != null ? c.getTitle() : "");
    m.put("brand", c.getBrandName() != null ? c.getBrandName() : "");
    m.put("brandId", c.getBrandUserId() != null ? c.getBrandUserId().toString() : "");
    m.put("platform", c.getPlatform() != null ? c.getPlatform() : "");
    m.put("price", c.getPricePaise() != null ? c.getPricePaise() / 100.0 : 0);
    m.put(
        "originalPrice", c.getOriginalPricePaise() != null ? c.getOriginalPricePaise() / 100.0 : 0);
    m.put("payout", payoutPaise / 100.0);
    m.put("image", c.getImage() != null ? c.getImage() : "");
    m.put("productUrl", c.getProductUrl() != null ? c.getProductUrl() : "");
    m.put("totalSlots", c.getTotalSlots() != null ? c.getTotalSlots() : 0);
    m.put("usedSlots", c.getUsedSlots() != null ? c.getUsedSlots() : 0);
    m.put(
        "status",
        statusMap.getOrDefault(c.getStatus() != null ? c.getStatus().toLowerCase() : "", "Draft"));
    m.put("assignments", assignments);
    m.put("assignmentDetails", assignmentDetails);
    m.put(
        "allowedAgencies", c.getAllowedAgencies() != null ? c.getAllowedAgencies() : new String[0]);
    m.put(
        "createdAt",
        c.getCreatedAt() != null ? c.getCreatedAt().toEpochMilli() : System.currentTimeMillis());
    m.put("returnWindowDays", c.getReturnWindowDays() != null ? c.getReturnWindowDays() : 14);
    m.put("dealType", c.getDealType() != null ? c.getDealType() : "");
    m.put("openToAll", Boolean.TRUE.equals(c.getOpenToAll()));

    // Per-mediator assignment info
    if (requesterMediatorCode != null && !requesterMediatorCode.isBlank()) {
      String codeKey = requesterMediatorCode.toLowerCase();
      Object det = assignmentDetails.get(codeKey);
      if (det instanceof Map<?, ?> detMap) {
        m.put("assignmentCommission", detMap.get("commission"));
        m.put("assignmentPayout", detMap.get("payout"));
      }
    }
    return m;
  }

  // ── Deal management ──────────────────────────────────────────────────────────

  @PostMapping("/deals/publish")
  @PreAuthorize("isAuthenticated()")
  @ResponseStatus(HttpStatus.CREATED)
  public Map<String, Object> publishDeal(
      @Valid @RequestBody PublishDealRequest req, @CurrentUserId UUID actorUserId) {
    int commissionPaise =
        (int) Math.round((req.commission() != null ? req.commission() : 0.0) * 100);
    return toUiDeal(
        opsService.publishDeal(req.id(), req.mediatorCode(), commissionPaise, actorUserId));
  }

  @PostMapping("/deals/{dealId}/decline")
  @PreAuthorize("hasAnyRole('ops','admin','mediator','agency')")
  @ResponseStatus(HttpStatus.NO_CONTENT)
  public void declineOffer(@PathVariable UUID dealId, @CurrentUserId UUID actorUserId) {
    opsService.declineOffer(dealId, actorUserId);
  }

  @GetMapping("/deals")
  @PreAuthorize("hasAnyRole('ops','admin','agency','mediator')")
  public List<Map<String, Object>> getDeals(
      @RequestParam(required = false) List<String> mediatorCodes,
      @RequestParam(defaultValue = "50") int limit,
      @RequestParam(defaultValue = "0") int offset,
      @CurrentUserId UUID actorId) {
    List<String> codes =
        (mediatorCodes != null && !mediatorCodes.isEmpty())
            ? mediatorCodes
            : resolveCode(null, actorId) != null ? List.of(resolveCode(null, actorId)) : List.of();
    return opsService.getDeals(codes, limit, offset).stream().map(OpsController::toUiDeal).toList();
  }

  static Map<String, Object> toUiDeal(DealsResponseDto d) {
    Map<String, Object> m = new LinkedHashMap<>();
    m.put("id", d.getId());
    m.put("campaignId", d.getCampaignId());
    m.put("mediatorCode", d.getMediatorCode() != null ? d.getMediatorCode() : "");
    m.put("title", d.getTitle() != null ? d.getTitle() : "");
    m.put("description", d.getDescription() != null ? d.getDescription() : "Exclusive");
    m.put("price", d.getPricePaise() != null ? d.getPricePaise() / 100.0 : 0);
    m.put(
        "originalPrice", d.getOriginalPricePaise() != null ? d.getOriginalPricePaise() / 100.0 : 0);
    m.put("commission", d.getCommissionPaise() != null ? d.getCommissionPaise() / 100.0 : 0);
    m.put("image", d.getImage() != null ? d.getImage() : "");
    m.put("productUrl", d.getProductUrl() != null ? d.getProductUrl() : "");
    m.put("platform", d.getPlatform() != null ? d.getPlatform() : "");
    m.put("brandName", d.getBrandName() != null ? d.getBrandName() : "");
    m.put("dealType", d.getDealType() != null ? d.getDealType() : "");
    m.put("rating", d.getRating() != null ? d.getRating() : 5.0);
    m.put("category", d.getCategory() != null ? d.getCategory() : "General");
    m.put("active", Boolean.TRUE.equals(d.getActive()));
    m.put("totalSlots", 0);
    m.put("usedSlots", 0);
    m.put("remainingSlots", 0);
    m.put("sellingSpeed", 0);
    return m;
  }

  // ── Order management ─────────────────────────────────────────────────────────

  @PostMapping("/orders/verify-claim")
  @PreAuthorize("hasAnyRole('ops','admin','mediator','agency')")
  public OrdersResponseDto verifyOrderClaim(
      @RequestBody Map<String, String> body, @CurrentUserId UUID actorUserId) {
    return opsService.verifyOrderClaim(UUID.fromString(body.get("orderId")), actorUserId);
  }

  @PostMapping("/orders/verify-requirement")
  @PreAuthorize("hasAnyRole('ops','admin','mediator','agency')")
  public OrdersResponseDto verifyOrderRequirement(
      @Valid @RequestBody VerifyRequirementRequest req, @CurrentUserId UUID actorUserId) {
    return opsService.verifyOrderRequirement(req.orderId(), req.type(), actorUserId);
  }

  @PostMapping("/orders/verify-all")
  @PreAuthorize("hasAnyRole('ops','admin','mediator','agency')")
  public OrdersResponseDto verifyAllSteps(
      @RequestBody Map<String, String> body, @CurrentUserId UUID actorUserId) {
    return opsService.verifyAllSteps(UUID.fromString(body.get("orderId")), actorUserId);
  }

  @PostMapping("/orders/reject-proof")
  @PreAuthorize("hasAnyRole('ops','admin','mediator','agency')")
  @ResponseStatus(HttpStatus.NO_CONTENT)
  public void rejectOrderProof(
      @Valid @RequestBody RejectProofRequest req, @CurrentUserId UUID actorUserId) {
    opsService.rejectOrderProof(req.orderId(), req.type(), req.reason(), actorUserId);
  }

  @PostMapping("/orders/cancel-proofs")
  @PreAuthorize("hasAnyRole('ops','admin','mediator','agency')")
  @ResponseStatus(HttpStatus.NO_CONTENT)
  public void cancelOrderProofs(
      @Valid @RequestBody CancelOrderProofsRequest req, @CurrentUserId UUID actorUserId) {
    opsService.cancelOrderProofs(req.orderId(), req.reason(), actorUserId);
  }

  @PostMapping("/orders/request-missing-proof")
  @PreAuthorize("hasAnyRole('ops','admin','mediator','agency')")
  @ResponseStatus(HttpStatus.NO_CONTENT)
  public void requestMissingProof(
      @Valid @RequestBody RequestMissingProofRequest req, @CurrentUserId UUID actorUserId) {
    opsService.requestMissingProof(req.orderId(), req.type(), req.note(), actorUserId);
  }

  @PostMapping("/orders/settle")
  @PreAuthorize("hasAnyRole('ops','admin','mediator','agency')")
  @ResponseStatus(HttpStatus.NO_CONTENT)
  public void settleOrderPayment(
      @Valid @RequestBody SettleOrderRequest req, @CurrentUserId UUID actorUserId) {
    opsService.settleOrderPayment(
        req.orderId(), req.settlementRef(), req.settlementMode(), actorUserId);
  }

  @PostMapping("/orders/unsettle")
  @PreAuthorize("hasAnyRole('ops','admin','mediator','agency')")
  @ResponseStatus(HttpStatus.NO_CONTENT)
  public void unsettleOrderPayment(
      @RequestBody Map<String, String> body, @CurrentUserId UUID actorUserId) {
    opsService.unsettleOrderPayment(UUID.fromString(body.get("orderId")), actorUserId);
  }

  @PostMapping("/orders/force-approve")
  @PreAuthorize("hasAnyRole('ops','admin')")
  @ResponseStatus(HttpStatus.NO_CONTENT)
  public void forceApproveOrder(
      @RequestBody Map<String, String> body, @CurrentUserId UUID actorUserId) {
    opsService.forceApproveOrder(UUID.fromString(body.get("orderId")), actorUserId);
  }

  @PostMapping("/orders/cancel")
  @PreAuthorize("hasAnyRole('ops','admin')")
  @ResponseStatus(HttpStatus.NO_CONTENT)
  public void cancelOrder(
      @Valid @RequestBody CancelOrderRequest req, @CurrentUserId UUID actorUserId) {
    opsService.cancelOrder(req.orderId(), req.reason(), actorUserId);
  }

  @GetMapping("/orders")
  @PreAuthorize("hasAnyRole('ops','admin','agency','mediator')")
  public List<OrdersResponseDto> getOrders(
      @RequestParam(required = false) List<String> managerCodes,
      @RequestParam(defaultValue = "50") int limit,
      @RequestParam(defaultValue = "0") int offset) {
    return opsService.getOrders(managerCodes, limit, offset);
  }

  // ── Mediator/user management ─────────────────────────────────────────────────

  @PostMapping("/mediators/approve")
  @PreAuthorize("hasAnyRole('ops','admin','agency')")
  @ResponseStatus(HttpStatus.NO_CONTENT)
  public void approveMediator(
      @RequestBody Map<String, String> body, @CurrentUserId UUID actorUserId) {
    opsService.approveMediator(UUID.fromString(body.get("id")), actorUserId);
  }

  @PostMapping("/mediators/reject")
  @PreAuthorize("hasAnyRole('ops','admin','agency')")
  @ResponseStatus(HttpStatus.NO_CONTENT)
  public void rejectMediator(
      @RequestBody Map<String, String> body, @CurrentUserId UUID actorUserId) {
    opsService.rejectMediator(UUID.fromString(body.get("id")), actorUserId);
  }

  @PostMapping("/users/approve")
  @PreAuthorize("hasAnyRole('ops','admin','mediator','agency')")
  @ResponseStatus(HttpStatus.NO_CONTENT)
  public void approveUser(@RequestBody Map<String, String> body, @CurrentUserId UUID actorUserId) {
    opsService.approveUser(UUID.fromString(body.get("id")), actorUserId);
  }

  @PostMapping("/users/reject")
  @PreAuthorize("hasAnyRole('ops','admin','mediator','agency')")
  @ResponseStatus(HttpStatus.NO_CONTENT)
  public void rejectUser(@RequestBody Map<String, String> body, @CurrentUserId UUID actorUserId) {
    opsService.rejectUser(UUID.fromString(body.get("id")), actorUserId);
  }

  @GetMapping("/mediators")
  @PreAuthorize("hasAnyRole('ops','admin','agency','mediator')")
  public List<UsersResponseDto> getMediators(
      @RequestParam(required = false) String agencyCode,
      @RequestParam(defaultValue = "50") int limit,
      @RequestParam(defaultValue = "0") int offset) {
    return opsService.getMediators(agencyCode, limit, offset);
  }

  @GetMapping("/users/pending")
  @PreAuthorize("hasAnyRole('ops','admin','mediator','agency')")
  public List<UsersResponseDto> getPendingUsers(
      @RequestParam(name = "code", required = false) String code,
      @RequestParam(name = "parentCode", required = false) String parentCode,
      @RequestParam(defaultValue = "50") int limit,
      @RequestParam(defaultValue = "0") int offset) {
    String resolvedCode = code != null ? code : (parentCode != null ? parentCode : "");
    return opsService.getPendingUsers(resolvedCode, limit, offset);
  }

  @GetMapping("/users/verified")
  @PreAuthorize("hasAnyRole('ops','admin','mediator','agency')")
  public List<UsersResponseDto> getVerifiedUsers(
      @RequestParam(name = "code", required = false) String code,
      @RequestParam(name = "parentCode", required = false) String parentCode,
      @RequestParam(defaultValue = "50") int limit,
      @RequestParam(defaultValue = "0") int offset) {
    String resolvedCode = code != null ? code : (parentCode != null ? parentCode : "");
    return opsService.getVerifiedUsers(resolvedCode, limit, offset);
  }

  // ── Payouts ──────────────────────────────────────────────────────────────────

  @PostMapping("/payouts")
  @ResponseStatus(HttpStatus.CREATED)
  @PreAuthorize("hasAnyRole('ops','admin')")
  public PayoutsResponseDto payoutMediator(
      @Valid @RequestBody PayoutMediatorRequest req, @CurrentUserId UUID actorUserId) {
    return opsService.payoutMediator(req.mediatorUserId(), req.amountPaise(), actorUserId);
  }

  @DeleteMapping("/payouts/{payoutId}")
  @ResponseStatus(HttpStatus.NO_CONTENT)
  @PreAuthorize("hasAnyRole('ops','admin')")
  public void deletePayout(@PathVariable UUID payoutId, @CurrentUserId UUID actorUserId) {
    opsService.deletePayout(payoutId, actorUserId);
  }

  @GetMapping("/ledger")
  @PreAuthorize("hasAnyRole('ops','admin','mediator','agency')")
  public List<PayoutsResponseDto> getLedger(
      @RequestParam(defaultValue = "100") int limit, @RequestParam(defaultValue = "0") int offset) {
    return opsService.getLedger(limit, offset);
  }

  // ── Dashboard ─────────────────────────────────────────────────────────────────

  @GetMapping("/dashboard")
  @PreAuthorize("hasAnyRole('ops','admin')")
  public Map<String, Object> getDashboardStats() {
    return opsService.getDashboardStats();
  }

  @GetMapping("/dashboard-stats")
  @PreAuthorize("hasAnyRole('ops','admin','agency')")
  public Map<String, Object> getAgencyDashboardStats(@RequestParam String agencyCode) {
    return opsService.getAgencyDashboardStats(agencyCode);
  }

  @GetMapping("/revenue-trend")
  @PreAuthorize("hasAnyRole('ops','admin','agency')")
  public List<Map<String, Object>> getRevenueTrend(
      @RequestParam String agencyCode, @RequestParam(defaultValue = "last7") String range) {
    return opsService.getRevenueTrend(agencyCode, range);
  }

  @GetMapping("/brand-performance")
  @PreAuthorize("hasAnyRole('ops','admin','agency')")
  public List<Map<String, Object>> getBrandPerformance(@RequestParam String agencyCode) {
    return opsService.getBrandPerformance(agencyCode);
  }

  // ── Invites ───────────────────────────────────────────────────────────────────

  @PostMapping("/invites/generate")
  @ResponseStatus(HttpStatus.CREATED)
  public Map<String, Object> generateMediatorInvite(
      @RequestBody Map<String, Object> body, @CurrentUserId UUID actorUserId) {
    Object agencyIdVal = body.get("agencyId");
    if (agencyIdVal == null || String.valueOf(agencyIdVal).isBlank()) {
      throw new ApiException(org.springframework.http.HttpStatus.BAD_REQUEST, "MISSING_AGENCY_ID");
    }
    UUID agencyId = UUID.fromString(String.valueOf(agencyIdVal));

    UsersEntity requester =
        usersRepository
            .findById(actorUserId)
            .orElseThrow(
                () ->
                    new ApiException(
                        org.springframework.http.HttpStatus.UNAUTHORIZED, "UNAUTHENTICATED"));

    String[] requesterRoles = requester.getRoles() != null ? requester.getRoles() : new String[0];
    boolean isPrivileged =
        Arrays.asList(requesterRoles).contains("admin")
            || Arrays.asList(requesterRoles).contains("ops");
    boolean isAgencySelf =
        Arrays.asList(requesterRoles).contains("agency") && actorUserId.equals(agencyId);
    if (!isAgencySelf && !isPrivileged) {
      throw new ApiException(org.springframework.http.HttpStatus.FORBIDDEN, "FORBIDDEN");
    }

    UsersEntity agency =
        usersRepository
            .findById(agencyId)
            .filter(u -> !Boolean.TRUE.equals(u.getIsDeleted()))
            .filter(u -> u.getRoles() != null && Arrays.asList(u.getRoles()).contains("agency"))
            .orElseThrow(
                () ->
                    new ApiException(
                        org.springframework.http.HttpStatus.NOT_FOUND, "AGENCY_NOT_FOUND"));

    String parentCode = agency.getMediatorCode();
    if (parentCode == null || parentCode.isBlank()) {
      parentCode =
          "AGY-" + UUID.randomUUID().toString().replace("-", "").substring(0, 8).toUpperCase();
      agency.setMediatorCode(parentCode);
      usersRepository.save(agency);
    }

    String code = generateUniqueInviteCode("INV");
    InvitesEntity invite = new InvitesEntity();
    invite.setCode(code);
    invite.setRole(UserRole.mediator);
    invite.setParentUserId(agency.getId());
    invite.setParentCode(parentCode);
    invite.setCreatedBy(actorUserId);
    invite.setExpiresAt(Instant.now().plusSeconds(60L * 60 * 24 * 14));
    invitesRepository.save(invite);

    return Map.of("code", code);
  }

  @PostMapping("/invites/generate-buyer")
  @ResponseStatus(HttpStatus.CREATED)
  public Map<String, Object> generateBuyerInvite(
      @RequestBody Map<String, Object> body, @CurrentUserId UUID actorUserId) {
    Object mediatorIdVal = body.get("mediatorId");
    if (mediatorIdVal == null || String.valueOf(mediatorIdVal).isBlank()) {
      throw new ApiException(
          org.springframework.http.HttpStatus.BAD_REQUEST, "MISSING_MEDIATOR_ID");
    }
    UUID mediatorId = UUID.fromString(String.valueOf(mediatorIdVal));

    UsersEntity requester =
        usersRepository
            .findById(actorUserId)
            .orElseThrow(
                () ->
                    new ApiException(
                        org.springframework.http.HttpStatus.UNAUTHORIZED, "UNAUTHENTICATED"));

    String[] requesterRoles = requester.getRoles() != null ? requester.getRoles() : new String[0];
    boolean isPrivileged =
        Arrays.asList(requesterRoles).contains("admin")
            || Arrays.asList(requesterRoles).contains("ops");
    boolean isMediatorSelf =
        Arrays.asList(requesterRoles).contains("mediator") && actorUserId.equals(mediatorId);
    if (!isMediatorSelf && !isPrivileged) {
      throw new ApiException(org.springframework.http.HttpStatus.FORBIDDEN, "FORBIDDEN");
    }

    UsersEntity mediator =
        usersRepository
            .findById(mediatorId)
            .filter(u -> !Boolean.TRUE.equals(u.getIsDeleted()))
            .filter(u -> u.getRoles() != null && Arrays.asList(u.getRoles()).contains("mediator"))
            .orElseThrow(
                () ->
                    new ApiException(
                        org.springframework.http.HttpStatus.NOT_FOUND, "MEDIATOR_NOT_FOUND"));

    String parentCode = mediator.getMediatorCode();
    if (parentCode == null || parentCode.isBlank()) {
      throw new ApiException(org.springframework.http.HttpStatus.CONFLICT, "MISSING_MEDIATOR_CODE");
    }

    String code = generateUniqueInviteCode("INV");
    InvitesEntity invite = new InvitesEntity();
    invite.setCode(code);
    invite.setRole(UserRole.shopper);
    invite.setParentUserId(mediator.getId());
    invite.setParentCode(parentCode);
    invite.setCreatedBy(actorUserId);
    invite.setExpiresAt(Instant.now().plusSeconds(60L * 60 * 24 * 14));
    invitesRepository.save(invite);

    return Map.of("code", code);
  }

  private String generateUniqueInviteCode(String prefix) {
    for (int i = 0; i < 10; i++) {
      String candidate =
          prefix
              + "-"
              + UUID.randomUUID().toString().replace("-", "").substring(0, 8).toUpperCase();
      if (!invitesRepository.existsByCode(candidate)) {
        return candidate;
      }
    }
    throw new ApiException(
        org.springframework.http.HttpStatus.INTERNAL_SERVER_ERROR, "CODE_GENERATION_FAILED");
  }

  // ── Connections ───────────────────────────────────────────────────────────────

  @PostMapping("/connections/brand")
  @PreAuthorize("hasAnyRole('agency','ops','admin')")
  @ResponseStatus(HttpStatus.NO_CONTENT)
  public void requestBrandConnection(
      @Valid @RequestBody RequestBrandConnectionRequest req, @CurrentUserId UUID actorUserId) {
    opsService.requestBrandConnection(
        req.brandCode(), req.agencyCode(), req.agencyName(), actorUserId);
  }

  // ── Inner request records ─────────────────────────────────────────────────────

  public record CreateCampaignRequest(
      UUID brandUserId,
      @NotBlank String title,
      @NotBlank String platform,
      @NotBlank String image,
      @NotBlank String productUrl,
      Double originalPrice,
      Integer originalPricePaise,
      Double price,
      Integer pricePaise,
      Double payout,
      Integer payoutPaise,
      @NotNull Integer totalSlots,
      String dealType,
      Integer returnWindowDays,
      String[] allowedAgencyCodes,
      String[] allowedAgencies,
      String brandName) {

    int resolvedOriginalPricePaise() {
      if (originalPricePaise != null) {
        return originalPricePaise;
      }
      return originalPrice != null ? (int) Math.round(originalPrice * 100) : 0;
    }

    int resolvedPricePaise() {
      if (pricePaise != null) {
        return pricePaise;
      }
      return price != null ? (int) Math.round(price * 100) : 0;
    }

    int resolvedPayoutPaise() {
      if (payoutPaise != null) {
        return payoutPaise;
      }
      return payout != null ? (int) Math.round(payout * 100) : 0;
    }

    String[] resolvedAllowedAgencyCodes() {
      if (allowedAgencyCodes != null) {
        return allowedAgencyCodes;
      }
      return allowedAgencies != null ? allowedAgencies : new String[0];
    }
  }

  public record PublishDealRequest(
      @NotNull UUID id, @NotBlank String mediatorCode, Double commission) {}

  public record VerifyRequirementRequest(@NotNull UUID orderId, @NotBlank String type) {}

  public record RejectProofRequest(@NotNull UUID orderId, @NotBlank String type, String reason) {}

  public record CancelOrderProofsRequest(@NotNull UUID orderId, String reason) {}

  public record RequestMissingProofRequest(
      @NotNull UUID orderId, @NotBlank String type, String note) {}

  public record SettleOrderRequest(
      @NotNull UUID orderId, String settlementRef, String settlementMode) {}

  public record CancelOrderRequest(@NotNull UUID orderId, String reason) {}

  public record PayoutMediatorRequest(@NotNull UUID mediatorUserId, @Positive int amountPaise) {}

  public record RequestBrandConnectionRequest(
      @NotBlank String brandCode, @NotBlank String agencyCode, String agencyName) {}
}
