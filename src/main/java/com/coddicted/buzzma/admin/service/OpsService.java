package com.coddicted.buzzma.admin.service;

import com.coddicted.buzzma.catalog.api.CampaignsResponseDto;
import com.coddicted.buzzma.catalog.api.DealsResponseDto;
import com.coddicted.buzzma.identity.api.UsersResponseDto;
import com.coddicted.buzzma.orders.api.OrdersResponseDto;
import com.coddicted.buzzma.wallet.api.PayoutsResponseDto;
import java.util.List;
import java.util.Map;
import java.util.UUID;

public interface OpsService {

  // ── Campaign CRUD ────────────────────────────────────────────────────────────

  CampaignsResponseDto createCampaign(
      UUID brandUserId,
      String brandName,
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
      UUID actorUserId);

  CampaignsResponseDto copyCampaign(UUID campaignId, UUID actorUserId);

  CampaignsResponseDto updateCampaignStatus(UUID campaignId, String status, UUID actorUserId);

  void deleteCampaign(UUID campaignId, UUID actorUserId);

  void assignSlots(UUID campaignId, Map<String, Object> assignments, UUID actorUserId);

  // ── Deal management ──────────────────────────────────────────────────────────

  DealsResponseDto publishDeal(UUID campaignId, String mediatorCode, UUID actorUserId);

  void declineOffer(UUID dealId, UUID actorUserId);

  // ── Order management ─────────────────────────────────────────────────────────

  void settleOrderPayment(
      UUID orderId, String settlementRef, String settlementMode, UUID actorUserId);

  void unsettleOrderPayment(UUID orderId, UUID actorUserId);

  OrdersResponseDto verifyOrderClaim(UUID orderId, UUID actorUserId);

  OrdersResponseDto verifyOrderRequirement(UUID orderId, String type, UUID actorUserId);

  OrdersResponseDto verifyAllSteps(UUID orderId, UUID actorUserId);

  void rejectOrderProof(UUID orderId, String type, String reason, UUID actorUserId);

  void cancelOrderProofs(UUID orderId, String reason, UUID actorUserId);

  void requestMissingProof(UUID orderId, String type, String note, UUID actorUserId);

  void forceApproveOrder(UUID orderId, UUID actorUserId);

  void cancelOrder(UUID orderId, String reason, UUID actorUserId);

  // ── Mediator management ──────────────────────────────────────────────────────

  void approveMediator(UUID mediatorUserId, UUID actorUserId);

  void rejectMediator(UUID mediatorUserId, UUID actorUserId);

  void approveUser(UUID userId, UUID actorUserId);

  void rejectUser(UUID userId, UUID actorUserId);

  PayoutsResponseDto payoutMediator(UUID mediatorUserId, int amountPaise, UUID actorUserId);

  void deletePayout(UUID payoutId, UUID actorUserId);

  // ── Dashboard / listing ──────────────────────────────────────────────────────

  Map<String, Object> getDashboardStats();

  Map<String, Object> getAgencyDashboardStats(String agencyCode);

  List<Map<String, Object>> getRevenueTrend(String agencyCode, String range);

  List<Map<String, Object>> getBrandPerformance(String agencyCode);

  List<UsersResponseDto> getMediators(String agencyCode, int limit, int offset);

  List<CampaignsResponseDto> getCampaigns(String mediatorCode, int limit, int offset);

  List<DealsResponseDto> getDeals(List<String> mediatorCodes, int limit, int offset);

  List<OrdersResponseDto> getOrders(List<String> managerCodes, int limit, int offset);

  List<UsersResponseDto> getPendingUsers(String parentCode, int limit, int offset);

  List<UsersResponseDto> getVerifiedUsers(String parentCode, int limit, int offset);

  List<PayoutsResponseDto> getLedger(int limit, int offset);

  // ── Connections ───────────────────────────────────────────────────────────────

  void requestBrandConnection(
      String brandCode, String agencyCode, String agencyName, UUID actorUserId);
}
