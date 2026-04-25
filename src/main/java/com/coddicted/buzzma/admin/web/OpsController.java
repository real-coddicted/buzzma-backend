package com.coddicted.buzzma.admin.web;

import com.coddicted.buzzma.admin.service.OpsService;
import com.coddicted.buzzma.catalog.api.CampaignsResponseDto;
import com.coddicted.buzzma.catalog.api.DealsResponseDto;
import com.coddicted.buzzma.identity.api.UsersResponseDto;
import com.coddicted.buzzma.orders.api.OrdersResponseDto;
import com.coddicted.buzzma.shared.security.CurrentUserId;
import com.coddicted.buzzma.wallet.api.PayoutsResponseDto;
import jakarta.validation.Valid;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Positive;
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
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.ResponseStatus;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/api/ops")
@PreAuthorize("isAuthenticated()")
public class OpsController {

  private final OpsService opsService;

  public OpsController(OpsService opsService) {
    this.opsService = opsService;
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
  public CampaignsResponseDto copyCampaign(
      @PathVariable UUID campaignId, @CurrentUserId UUID actorUserId) {
    return opsService.copyCampaign(campaignId, actorUserId);
  }

  @PostMapping("/campaigns/{campaignId}/status")
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

  @GetMapping("/campaigns")
  @PreAuthorize("hasAnyRole('ops','admin','agency','mediator')")
  public List<CampaignsResponseDto> getCampaigns(
      @RequestParam(required = false) String mediatorCode,
      @RequestParam(defaultValue = "50") int limit,
      @RequestParam(defaultValue = "0") int offset) {
    return opsService.getCampaigns(mediatorCode, limit, offset);
  }

  // ── Deal management ──────────────────────────────────────────────────────────

  @PostMapping("/deals/publish")
  @PreAuthorize("hasAnyRole('ops','admin','agency')")
  @ResponseStatus(HttpStatus.CREATED)
  public DealsResponseDto publishDeal(
      @Valid @RequestBody PublishDealRequest req, @CurrentUserId UUID actorUserId) {
    return opsService.publishDeal(req.campaignId(), req.mediatorCode(), actorUserId);
  }

  @PostMapping("/deals/{dealId}/decline")
  @PreAuthorize("hasAnyRole('ops','admin','mediator','agency')")
  @ResponseStatus(HttpStatus.NO_CONTENT)
  public void declineOffer(@PathVariable UUID dealId, @CurrentUserId UUID actorUserId) {
    opsService.declineOffer(dealId, actorUserId);
  }

  @GetMapping("/deals")
  @PreAuthorize("hasAnyRole('ops','admin','agency','mediator')")
  public List<DealsResponseDto> getDeals(
      @RequestParam(required = false) List<String> mediatorCodes,
      @RequestParam(defaultValue = "50") int limit,
      @RequestParam(defaultValue = "0") int offset) {
    return opsService.getDeals(mediatorCodes, limit, offset);
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
      @RequestParam String parentCode,
      @RequestParam(defaultValue = "50") int limit,
      @RequestParam(defaultValue = "0") int offset) {
    return opsService.getPendingUsers(parentCode, limit, offset);
  }

  @GetMapping("/users/verified")
  @PreAuthorize("hasAnyRole('ops','admin','mediator','agency')")
  public List<UsersResponseDto> getVerifiedUsers(
      @RequestParam String parentCode,
      @RequestParam(defaultValue = "50") int limit,
      @RequestParam(defaultValue = "0") int offset) {
    return opsService.getVerifiedUsers(parentCode, limit, offset);
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
      if (originalPricePaise != null) return originalPricePaise;
      return originalPrice != null ? (int) Math.round(originalPrice * 100) : 0;
    }

    int resolvedPricePaise() {
      if (pricePaise != null) return pricePaise;
      return price != null ? (int) Math.round(price * 100) : 0;
    }

    int resolvedPayoutPaise() {
      if (payoutPaise != null) return payoutPaise;
      return payout != null ? (int) Math.round(payout * 100) : 0;
    }

    String[] resolvedAllowedAgencyCodes() {
      if (allowedAgencyCodes != null) return allowedAgencyCodes;
      return allowedAgencies != null ? allowedAgencies : new String[0];
    }
  }

  public record PublishDealRequest(@NotNull UUID campaignId, @NotBlank String mediatorCode) {}

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
