package com.coddicted.buzzma.admin.service.impl;

import com.coddicted.buzzma.admin.service.OpsService;
import com.coddicted.buzzma.catalog.api.CampaignsResponseDto;
import com.coddicted.buzzma.catalog.api.CatalogAdminPort;
import com.coddicted.buzzma.catalog.api.CatalogQueryPort;
import com.coddicted.buzzma.catalog.api.DealsResponseDto;
import com.coddicted.buzzma.identity.api.UserAdminPort;
import com.coddicted.buzzma.identity.api.UserQueryPort;
import com.coddicted.buzzma.identity.api.UsersResponseDto;
import com.coddicted.buzzma.mediator.api.MediatorAdminPort;
import com.coddicted.buzzma.orders.api.OrderAdminPort;
import com.coddicted.buzzma.orders.api.OrderQueryPort;
import com.coddicted.buzzma.orders.api.OrdersResponseDto;
import com.coddicted.buzzma.shared.common.AuditLogWriter;
import com.coddicted.buzzma.shared.enums.KycStatus;
import com.coddicted.buzzma.shared.enums.UserStatus;
import com.coddicted.buzzma.shared.exception.ApiException;
import com.coddicted.buzzma.wallet.api.PayoutsResponseDto;
import com.coddicted.buzzma.wallet.api.WalletAdminPort;
import com.coddicted.buzzma.wallet.api.WalletQueryPort;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.UUID;
import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
public class OpsServiceImpl implements OpsService {

  private final CatalogAdminPort catalogAdminPort;
  private final CatalogQueryPort catalogQueryPort;
  private final OrderAdminPort orderAdminPort;
  private final OrderQueryPort orderQueryPort;
  private final WalletAdminPort walletAdminPort;
  private final WalletQueryPort walletQueryPort;
  private final MediatorAdminPort mediatorAdminPort;
  private final UserAdminPort userAdminPort;
  private final UserQueryPort userQueryPort;
  private final AuditLogWriter auditLogWriter;

  public OpsServiceImpl(
      CatalogAdminPort catalogAdminPort,
      CatalogQueryPort catalogQueryPort,
      OrderAdminPort orderAdminPort,
      OrderQueryPort orderQueryPort,
      WalletAdminPort walletAdminPort,
      WalletQueryPort walletQueryPort,
      MediatorAdminPort mediatorAdminPort,
      UserAdminPort userAdminPort,
      UserQueryPort userQueryPort,
      AuditLogWriter auditLogWriter) {
    this.catalogAdminPort = catalogAdminPort;
    this.catalogQueryPort = catalogQueryPort;
    this.orderAdminPort = orderAdminPort;
    this.orderQueryPort = orderQueryPort;
    this.walletAdminPort = walletAdminPort;
    this.walletQueryPort = walletQueryPort;
    this.mediatorAdminPort = mediatorAdminPort;
    this.userAdminPort = userAdminPort;
    this.userQueryPort = userQueryPort;
    this.auditLogWriter = auditLogWriter;
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
    CampaignsResponseDto saved =
        catalogAdminPort.createCampaign(
            brandUserId,
            null,
            title,
            platform,
            image,
            productUrl,
            originalPricePaise,
            pricePaise,
            payoutPaise,
            totalSlots,
            dealType,
            returnWindowDays,
            allowedAgencyCodes,
            actorUserId);
    auditLogWriter.write(
        actorUserId,
        new String[] {"ops"},
        "CAMPAIGN_CREATED",
        "Campaign",
        saved.getId().toString(),
        null);
    return saved;
  }

  @Override
  @Transactional
  public CampaignsResponseDto copyCampaign(UUID campaignId, UUID actorUserId) {
    CampaignsResponseDto saved = catalogAdminPort.copyCampaign(campaignId, actorUserId);
    auditLogWriter.write(
        actorUserId,
        new String[] {"ops"},
        "CAMPAIGN_COPIED",
        "Campaign",
        saved.getId().toString(),
        "{\"sourceCampaignId\":\"" + campaignId + "\"}");
    return saved;
  }

  @Override
  @Transactional
  public CampaignsResponseDto updateCampaignStatus(
      UUID campaignId, String status, UUID actorUserId) {
    CampaignsResponseDto saved =
        catalogAdminPort.updateCampaignStatus(campaignId, status, actorUserId);
    auditLogWriter.write(
        actorUserId,
        new String[] {"ops"},
        "CAMPAIGN_STATUS_CHANGED",
        "Campaign",
        campaignId.toString(),
        "{\"status\":\"" + status + "\"}");
    return saved;
  }

  @Override
  @Transactional
  public void deleteCampaign(UUID campaignId, UUID actorUserId) {
    // Block deletion if campaign has orders
    if (orderAdminPort.countByCampaignId(campaignId) > 0) {
      throw new ApiException(HttpStatus.CONFLICT, "CAMPAIGN_HAS_ORDERS");
    }
    catalogAdminPort.deleteCampaign(campaignId, actorUserId);
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
    catalogAdminPort.assignSlots(campaignId, assignments, actorUserId);
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
    DealsResponseDto saved = catalogAdminPort.publishDeal(campaignId, mediatorCode, actorUserId);
    auditLogWriter.write(
        actorUserId,
        new String[] {"ops"},
        "DEAL_PUBLISHED",
        "Deal",
        saved.getId().toString(),
        "{\"campaignId\":\"" + campaignId + "\",\"mediatorCode\":\"" + mediatorCode + "\"}");
    return saved;
  }

  @Override
  @Transactional
  public void declineOffer(UUID dealId, UUID actorUserId) {
    catalogAdminPort.declineOffer(dealId, actorUserId);
    auditLogWriter.write(
        actorUserId, new String[] {"ops"}, "DEAL_DECLINED", "Deal", dealId.toString(), null);
  }

  // ── Order verification ────────────────────────────────────────────────────────

  @Override
  @Transactional
  public OrdersResponseDto verifyOrderClaim(UUID orderId, UUID actorUserId) {
    OrdersResponseDto saved = orderAdminPort.verifyOrderClaim(orderId, actorUserId);
    auditLogWriter.write(
        actorUserId,
        new String[] {"ops"},
        "ORDER_VERIFIED",
        "Order",
        orderId.toString(),
        "{\"step\":\"order\"}");
    return saved;
  }

  @Override
  @Transactional
  public OrdersResponseDto verifyOrderRequirement(UUID orderId, String type, UUID actorUserId) {
    OrdersResponseDto saved = orderAdminPort.verifyRequirement(orderId, type, actorUserId);
    auditLogWriter.write(
        actorUserId,
        new String[] {"ops"},
        "ORDER_VERIFIED",
        "Order",
        orderId.toString(),
        "{\"step\":\"" + type + "\"}");
    return saved;
  }

  @Override
  @Transactional
  public OrdersResponseDto verifyAllSteps(UUID orderId, UUID actorUserId) {
    OrdersResponseDto saved = orderAdminPort.verifyAllSteps(orderId, actorUserId);
    auditLogWriter.write(
        actorUserId,
        new String[] {"ops"},
        "ORDER_ALL_STEPS_VERIFIED",
        "Order",
        orderId.toString(),
        null);
    return saved;
  }

  @Override
  @Transactional
  public void rejectOrderProof(UUID orderId, String type, String reason, UUID actorUserId) {
    orderAdminPort.rejectProof(orderId, type, reason, actorUserId);
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
    orderAdminPort.cancelAllProofs(orderId, reason, actorUserId);
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
    orderAdminPort.requestMissingProof(orderId, type, note, actorUserId);
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
    orderAdminPort.forceApprove(orderId, actorUserId);
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
    orderAdminPort.cancelOrder(orderId, reason, actorUserId);
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
    orderAdminPort.settleOrderPayment(orderId, settlementRef, settlementMode, actorUserId);
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
    orderAdminPort.unsettleOrderPayment(orderId, actorUserId);
    auditLogWriter.write(
        actorUserId, new String[] {"ops"}, "ORDER_UNSETTLED", "Order", orderId.toString(), null);
  }

  // ── Mediator management ──────────────────────────────────────────────────────

  @Override
  @Transactional
  public void approveMediator(UUID mediatorUserId, UUID actorUserId) {
    userAdminPort.setKycStatus(mediatorUserId, KycStatus.verified, actorUserId);
    userAdminPort.setStatus(mediatorUserId, UserStatus.active, actorUserId);
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
    userAdminPort.setKycStatus(mediatorUserId, KycStatus.rejected, actorUserId);
    userAdminPort.setStatus(mediatorUserId, UserStatus.suspended, actorUserId);
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
    userAdminPort.setIsVerifiedByMediator(userId, true, actorUserId);
    auditLogWriter.write(
        actorUserId, new String[] {"ops"}, "BUYER_APPROVED", "User", userId.toString(), null);
  }

  @Override
  @Transactional
  public void rejectUser(UUID userId, UUID actorUserId) {
    userAdminPort.setStatus(userId, UserStatus.suspended, actorUserId);
    auditLogWriter.write(
        actorUserId, new String[] {"ops"}, "USER_REJECTED", "User", userId.toString(), null);
  }

  @Override
  @Transactional
  public PayoutsResponseDto payoutMediator(UUID mediatorUserId, int amountPaise, UUID actorUserId) {
    // Ensure beneficiary user exists and is not deleted
    userQueryPort
        .findById(mediatorUserId)
        .orElseThrow(() -> new ApiException(HttpStatus.NOT_FOUND, "USER_NOT_FOUND"));

    PayoutsResponseDto saved =
        walletAdminPort.createPayout(mediatorUserId, amountPaise, actorUserId);
    auditLogWriter.write(
        actorUserId,
        new String[] {"ops"},
        "MEDIATOR_PAYOUT",
        "Payout",
        saved.getId().toString(),
        "{\"mediatorUserId\":\"" + mediatorUserId + "\",\"amountPaise\":" + amountPaise + "}");
    return saved;
  }

  @Override
  @Transactional
  public void deletePayout(UUID payoutId, UUID actorUserId) {
    walletAdminPort.deletePayout(payoutId, actorUserId);
    auditLogWriter.write(
        actorUserId, new String[] {"ops"}, "PAYOUT_DELETED", "Payout", payoutId.toString(), null);
  }

  // ── Dashboard / listing ──────────────────────────────────────────────────────

  @Override
  @Transactional(readOnly = true)
  public Map<String, Object> getDashboardStats() {
    Map<String, Object> stats = new HashMap<>();
    stats.put("totalOrders", orderQueryPort.count());
    stats.put("activeCampaigns", catalogQueryPort.countActiveCampaigns());
    stats.put("totalUsers", userQueryPort.count());
    return stats;
  }

  @Override
  @Transactional(readOnly = true)
  public List<UsersResponseDto> getMediators(String agencyCode, int limit, int offset) {
    return userQueryPort.listByParentCode(agencyCode, limit, offset);
  }

  @Override
  @Transactional(readOnly = true)
  public List<CampaignsResponseDto> getCampaigns(String mediatorCode, int limit, int offset) {
    return catalogQueryPort.listCampaigns(limit, offset);
  }

  @Override
  @Transactional(readOnly = true)
  public List<DealsResponseDto> getDeals(List<String> mediatorCodes, int limit, int offset) {
    return catalogQueryPort.listDealsByMediatorCodes(mediatorCodes, limit, offset);
  }

  @Override
  @Transactional(readOnly = true)
  public List<OrdersResponseDto> getOrders(List<String> managerCodes, int limit, int offset) {
    return orderQueryPort.listByManagerNames(managerCodes, limit, offset);
  }

  @Override
  @Transactional(readOnly = true)
  public List<UsersResponseDto> getPendingUsers(String parentCode, int limit, int offset) {
    return userQueryPort.listByParentCodeAndVerified(parentCode, false, limit, offset);
  }

  @Override
  @Transactional(readOnly = true)
  public List<UsersResponseDto> getVerifiedUsers(String parentCode, int limit, int offset) {
    return userQueryPort.listByParentCodeAndVerified(parentCode, true, limit, offset);
  }

  @Override
  @Transactional(readOnly = true)
  public List<PayoutsResponseDto> getLedger(int limit, int offset) {
    return walletQueryPort.listPayouts(limit, offset);
  }

  // ── Connections ───────────────────────────────────────────────────────────────

  @Override
  @Transactional
  public void requestBrandConnection(
      String brandCode, String agencyCode, String agencyName, UUID actorUserId) {
    Optional<UsersResponseDto> brandOpt = userQueryPort.findByMediatorCode(brandCode);
    UsersResponseDto brand =
        brandOpt.orElseThrow(() -> new ApiException(HttpStatus.NOT_FOUND, "BRAND_NOT_FOUND"));

    if (!UserStatus.active.name().equals(brand.getStatus())) {
      throw new ApiException(HttpStatus.CONFLICT, "BRAND_SUSPENDED");
    }

    String[] connected =
        brand.getConnectedAgencies() != null ? brand.getConnectedAgencies() : new String[0];
    for (String c : connected) {
      if (c.equals(agencyCode)) {
        throw new ApiException(HttpStatus.CONFLICT, "ALREADY_CONNECTED");
      }
    }

    mediatorAdminPort.requestBrandConnection(
        brand.getId(), brandCode, agencyCode, agencyName, actorUserId);

    auditLogWriter.write(
        actorUserId,
        new String[] {"agency"},
        "BRAND_CONNECTION_REQUESTED",
        "User",
        brand.getId().toString(),
        "{\"agencyCode\":\"" + agencyCode + "\",\"brandCode\":\"" + brandCode + "\"}");
  }

  // ── Private helpers ───────────────────────────────────────────────────────────

  private String escapeJson(String s) {
    return s.replace("\\", "\\\\").replace("\"", "\\\"");
  }
}
