package com.coddicted.buzzma.catalog.utils;

import com.coddicted.buzzma.catalog.persistence.CampaignsEntity;
import com.coddicted.buzzma.shared.enums.CampaignStatus;
import java.util.UUID;

public final class CampaignsUtil {
  private CampaignsUtil() {}

  public static CampaignsEntity copy(final CampaignsEntity source, final UUID actorUserId) {
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
    return copy;
  }
}
