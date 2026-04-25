package com.coddicted.buzzma.catalog.service.impl;

import com.coddicted.buzzma.catalog.api.CampaignsResponseDto;
import com.coddicted.buzzma.catalog.api.CatalogAdminPort;
import com.coddicted.buzzma.catalog.api.DealsResponseDto;
import com.coddicted.buzzma.catalog.mapper.CampaignsMapper;
import com.coddicted.buzzma.catalog.mapper.DealsMapper;
import com.coddicted.buzzma.catalog.persistence.CampaignsEntity;
import com.coddicted.buzzma.catalog.persistence.CampaignsRepository;
import com.coddicted.buzzma.catalog.persistence.DealsEntity;
import com.coddicted.buzzma.catalog.persistence.DealsRepository;
import com.coddicted.buzzma.identity.api.UserQueryPort;
import com.coddicted.buzzma.identity.api.UsersResponseDto;
import com.coddicted.buzzma.shared.enums.CampaignStatus;
import com.coddicted.buzzma.shared.enums.DealType;
import com.coddicted.buzzma.shared.exception.ApiException;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.node.ObjectNode;
import java.time.Instant;
import java.util.Map;
import java.util.Optional;
import java.util.UUID;
import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
public class CatalogAdminAdapter implements CatalogAdminPort {

  private final CampaignsRepository campaignsRepository;
  private final DealsRepository dealsRepository;
  private final CampaignsMapper campaignsMapper;
  private final DealsMapper dealsMapper;
  private final UserQueryPort userQueryPort;
  private final ObjectMapper objectMapper;

  public CatalogAdminAdapter(
      CampaignsRepository campaignsRepository,
      DealsRepository dealsRepository,
      CampaignsMapper campaignsMapper,
      DealsMapper dealsMapper,
      UserQueryPort userQueryPort,
      ObjectMapper objectMapper) {
    this.campaignsRepository = campaignsRepository;
    this.dealsRepository = dealsRepository;
    this.campaignsMapper = campaignsMapper;
    this.dealsMapper = dealsMapper;
    this.userQueryPort = userQueryPort;
    this.objectMapper = objectMapper;
  }

  @Override
  @Transactional
  public CampaignsResponseDto createCampaign(
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
      UUID actorUserId) {

    CampaignsEntity campaign = new CampaignsEntity();
    campaign.setBrandUserId(brandUserId != null ? brandUserId : actorUserId);
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
        campaign.setDealType(DealType.valueOf(dealType));
      } catch (IllegalArgumentException ignored) {
        // default kept
      }
    }
    campaign.setCreatedBy(actorUserId);
    campaign.setUpdatedBy(actorUserId);

    UUID resolvedBrandId = brandUserId != null ? brandUserId : actorUserId;
    Optional<UsersResponseDto> brand = userQueryPort.findById(resolvedBrandId);
    if (brand.isPresent()) {
      String name = brand.get().getName();
      campaign.setBrandName(name != null && !name.isBlank() ? name : (brandNameFallback != null ? brandNameFallback : "Brand"));
    } else {
      campaign.setBrandName(brandNameFallback != null ? brandNameFallback : "Brand");
    }

    CampaignsEntity saved = campaignsRepository.save(campaign);
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

    campaign.setIsDeleted(true);
    campaign.setUpdatedBy(actorUserId);
    campaignsRepository.save(campaign);

    // Deactivate associated deals (placeholder; deal soft-delete handled separately)
    dealsRepository
        .findAllByMediatorCodeAndIsDeletedFalseAndActiveTrue("")
        .forEach(
            d -> {
              // no-op placeholder — deal soft-delete handled separately via deal service
            });
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
  }

  @Override
  @Transactional
  public DealsResponseDto publishDeal(UUID campaignId, String mediatorCode, int commissionPaise, UUID actorUserId) {
    CampaignsEntity campaign =
        campaignsRepository
            .findById(campaignId)
            .filter(c -> !Boolean.TRUE.equals(c.getIsDeleted()))
            .orElseThrow(() -> new ApiException(HttpStatus.NOT_FOUND, "CAMPAIGN_NOT_FOUND"));

    if (campaign.getStatus() != CampaignStatus.active) {
      throw new ApiException(HttpStatus.CONFLICT, "CAMPAIGN_NOT_ACTIVE");
    }

    Optional<DealsEntity> existing =
        dealsRepository.findByCampaignIdAndMediatorCode(campaignId, mediatorCode);
    if (existing.isPresent() && !Boolean.TRUE.equals(existing.get().getIsDeleted())) {
      // Update commission on re-publish
      DealsEntity existingDeal = existing.get();
      existingDeal.setCommissionPaise(commissionPaise);
      existingDeal.setPricePaise(
          (campaign.getPricePaise() != null ? campaign.getPricePaise() : 0) + commissionPaise);
      existingDeal.setUpdatedBy(actorUserId);
      return dealsMapper.toResponse(dealsRepository.save(existingDeal));
    }

    int effectivePricePaise =
        (campaign.getPricePaise() != null ? campaign.getPricePaise() : 0) + commissionPaise;

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
    deal.setPricePaise(effectivePricePaise);
    deal.setPayoutPaise(campaign.getPayoutPaise());
    deal.setCommissionPaise(commissionPaise);
    deal.setActive(true);
    deal.setCreatedBy(actorUserId);
    deal.setUpdatedBy(actorUserId);

    DealsEntity saved = dealsRepository.save(deal);
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
  }

  @Override
  @Transactional(readOnly = true)
  public CampaignStatus getCampaignStatus(UUID campaignId) {
    CampaignsEntity campaign =
        campaignsRepository
            .findById(campaignId)
            .filter(c -> !Boolean.TRUE.equals(c.getIsDeleted()))
            .orElseThrow(() -> new ApiException(HttpStatus.NOT_FOUND, "CAMPAIGN_NOT_FOUND"));
    return campaign.getStatus();
  }
}
