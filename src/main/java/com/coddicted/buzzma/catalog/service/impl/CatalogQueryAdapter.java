package com.coddicted.buzzma.catalog.service.impl;

import com.coddicted.buzzma.catalog.api.CampaignsResponseDto;
import com.coddicted.buzzma.catalog.api.CatalogQueryPort;
import com.coddicted.buzzma.catalog.api.DealsResponseDto;
import com.coddicted.buzzma.catalog.mapper.CampaignsMapper;
import com.coddicted.buzzma.catalog.mapper.DealsMapper;
import com.coddicted.buzzma.catalog.persistence.CampaignsRepository;
import com.coddicted.buzzma.catalog.persistence.DealsRepository;
import com.coddicted.buzzma.shared.common.OffsetBasedPageRequest;
import java.util.List;
import java.util.Optional;
import java.util.UUID;
import java.util.stream.Collectors;
import org.springframework.data.domain.Sort;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
public class CatalogQueryAdapter implements CatalogQueryPort {

  private final DealsRepository dealsRepository;
  private final CampaignsRepository campaignsRepository;
  private final DealsMapper dealsMapper;
  private final CampaignsMapper campaignsMapper;

  public CatalogQueryAdapter(
      DealsRepository dealsRepository,
      CampaignsRepository campaignsRepository,
      DealsMapper dealsMapper,
      CampaignsMapper campaignsMapper) {
    this.dealsRepository = dealsRepository;
    this.campaignsRepository = campaignsRepository;
    this.dealsMapper = dealsMapper;
    this.campaignsMapper = campaignsMapper;
  }

  @Override
  @Transactional(readOnly = true)
  public Optional<DealsResponseDto> findDealById(UUID dealId) {
    return dealsRepository.findById(dealId).map(dealsMapper::toResponse);
  }

  @Override
  @Transactional(readOnly = true)
  public List<DealsResponseDto> listDealsByMediatorCodes(
      List<String> mediatorCodes, int limit, int offset) {
    if (mediatorCodes == null || mediatorCodes.isEmpty()) {
      return List.of();
    }
    var pageable =
        new OffsetBasedPageRequest(limit, offset, Sort.by(Sort.Direction.DESC, "createdAt"));
    return dealsRepository
        .findAllByMediatorCodeInAndIsDeletedFalse(mediatorCodes, pageable)
        .stream()
        .map(dealsMapper::toResponse)
        .collect(Collectors.toList());
  }

  @Override
  @Transactional(readOnly = true)
  public List<CampaignsResponseDto> listCampaigns(int limit, int offset) {
    var pageable =
        new OffsetBasedPageRequest(limit, offset, Sort.by(Sort.Direction.DESC, "createdAt"));
    return campaignsRepository.findAllByIsDeletedFalse(pageable).stream()
        .map(campaignsMapper::toResponse)
        .collect(Collectors.toList());
  }

  @Override
  @Transactional(readOnly = true)
  public long countActiveCampaigns() {
    return campaignsRepository.count();
  }
}
