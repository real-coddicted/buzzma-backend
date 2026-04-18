package com.mobo.catalog.api;

import com.mobo.shared.enums.CampaignStatus;
import java.util.Map;
import java.util.UUID;

public interface CatalogAdminPort {

  CampaignsResponseDto createCampaign(
      UUID brandUserId,
      String brandNameFallback,
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

  DealsResponseDto publishDeal(UUID campaignId, String mediatorCode, UUID actorUserId);

  void declineOffer(UUID dealId, UUID actorUserId);

  CampaignStatus getCampaignStatus(UUID campaignId);
}
