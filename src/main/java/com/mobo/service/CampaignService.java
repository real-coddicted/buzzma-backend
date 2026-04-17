package com.mobo.service;

import com.mobo.dto.CampaignsRequestDto;
import com.mobo.dto.CampaignsResponseDto;
import java.util.List;
import java.util.UUID;

public interface CampaignService {

  List<CampaignsResponseDto> list(int limit, int offset);

  CampaignsResponseDto getById(UUID id);

  CampaignsResponseDto create(CampaignsRequestDto request);

  CampaignsResponseDto update(UUID id, CampaignsRequestDto request);

  void delete(UUID id);
}
