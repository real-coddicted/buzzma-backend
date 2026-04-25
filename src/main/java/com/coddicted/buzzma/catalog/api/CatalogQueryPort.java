package com.coddicted.buzzma.catalog.api;

import java.util.List;
import java.util.Optional;
import java.util.UUID;

public interface CatalogQueryPort {

  Optional<DealsResponseDto> findDealById(UUID dealId);

  List<DealsResponseDto> listDealsByMediatorCodes(
      List<String> mediatorCodes, int limit, int offset);

  List<CampaignsResponseDto> listCampaigns(int limit, int offset);

  long countActiveCampaigns();

  long countActiveCampaignsForAgency(String agencyCode, List<String> managerCodes);
}
