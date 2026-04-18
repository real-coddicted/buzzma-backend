package com.mobo.catalog.service.impl;

import com.mobo.catalog.api.CampaignsRequestDto;
import com.mobo.catalog.api.CampaignsResponseDto;
import com.mobo.catalog.mapper.CampaignsMapper;
import com.mobo.catalog.persistence.CampaignsEntity;
import com.mobo.catalog.persistence.CampaignsRepository;
import com.mobo.catalog.service.CampaignService;
import com.mobo.shared.common.BaseCrudService;
import com.mobo.shared.common.OffsetBasedPageRequest;
import java.util.List;
import java.util.UUID;
import java.util.stream.Collectors;
import org.springframework.data.domain.Sort;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
public class CampaignServiceImpl extends BaseCrudService implements CampaignService {

  private final CampaignsRepository repository;
  private final CampaignsMapper mapper;

  public CampaignServiceImpl(CampaignsRepository repository, CampaignsMapper mapper) {
    this.repository = repository;
    this.mapper = mapper;
  }

  @Override
  @Transactional(readOnly = true)
  public List<CampaignsResponseDto> list(int limit, int offset) {
    var pageable =
        new OffsetBasedPageRequest(limit, offset, Sort.by(Sort.Direction.DESC, "createdAt"));
    return repository.findAllByIsDeletedFalse(pageable).stream()
        .map(mapper::toResponse)
        .collect(Collectors.toList());
  }

  @Override
  @Transactional(readOnly = true)
  public CampaignsResponseDto getById(UUID id) {
    CampaignsEntity entity = mustFind(repository, id, "Campaigns");
    return mapper.toResponse(entity);
  }

  @Override
  @Transactional
  public CampaignsResponseDto create(CampaignsRequestDto request) {
    CampaignsEntity entity = mapper.toEntity(request);
    return mapper.toResponse(repository.save(entity));
  }

  @Override
  @Transactional
  public CampaignsResponseDto update(UUID id, CampaignsRequestDto request) {
    CampaignsEntity entity = mustFind(repository, id, "Campaigns");
    mapper.update(request, entity);
    return mapper.toResponse(repository.save(entity));
  }

  @Override
  @Transactional
  public void delete(UUID id) {
    CampaignsEntity entity = mustFind(repository, id, "Campaigns");
    entity.setIsDeleted(true);
    repository.save(entity);
  }
}
