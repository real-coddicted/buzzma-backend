package com.coddicted.buzzma.catalog.service;

import com.coddicted.buzzma.catalog.api.CampaignsRequestDto;
import com.coddicted.buzzma.catalog.api.CampaignsResponseDto;
import java.util.List;
import java.util.UUID;

public interface CampaignService {

  List<CampaignsResponseDto> list(int limit, int offset);

  CampaignsResponseDto getById(UUID id);

  CampaignsResponseDto create(CampaignsRequestDto request);

  CampaignsResponseDto update(UUID id, CampaignsRequestDto request);

  void delete(UUID id);
}
