package com.coddicted.buzzma.service;

import com.coddicted.buzzma.common.BaseCrudService;
import com.coddicted.buzzma.common.OffsetBasedPageRequest;
import com.coddicted.buzzma.dto.CampaignsRequestDto;
import com.coddicted.buzzma.dto.CampaignsResponseDto;
import com.coddicted.buzzma.entity.CampaignsEntity;
import com.coddicted.buzzma.mapper.CampaignsMapper;
import com.coddicted.buzzma.repository.CampaignsRepository;
import java.util.List;
import java.util.UUID;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Sort;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
public class CampaignsService
    extends BaseCrudService<CampaignsEntity, CampaignsRequestDto, CampaignsResponseDto> {
  private final CampaignsRepository repository;
  private final CampaignsMapper mapper;

  public CampaignsService(final CampaignsRepository repository, final CampaignsMapper mapper) {
    this.repository = repository;
    this.mapper = mapper;
  }

  @Transactional(readOnly = true)
  public List<CampaignsResponseDto> list(final int limit, final int offset) {
    final Pageable pageable =
        new OffsetBasedPageRequest(limit, offset, Sort.by(Sort.Direction.DESC, "createdAt"));
    return repository.findAll(pageable).getContent().stream().map(mapper::toResponse).toList();
  }

  @Transactional(readOnly = true)
  public CampaignsResponseDto getById(final UUID id) {
    final CampaignsEntity entity = mustFind(repository, id, "campaigns");
    return mapper.toResponse(entity);
  }

  @Transactional
  public CampaignsResponseDto create(final CampaignsRequestDto request) {
    final CampaignsEntity entity = mapper.toEntity(request);
    final CampaignsEntity saved = repository.save(entity);
    return mapper.toResponse(saved);
  }

  @Transactional
  public CampaignsResponseDto update(final UUID id, final CampaignsRequestDto request) {
    final CampaignsEntity existing = mustFind(repository, id, "campaigns");
    mapper.update(request, existing);
    final CampaignsEntity saved = repository.save(existing);
    return mapper.toResponse(saved);
  }

  @Transactional
  public void delete(final UUID id) {
    final CampaignsEntity existing = mustFind(repository, id, "campaigns");
    existing.setIsDeleted(true);
    repository.save(existing);
  }
}
