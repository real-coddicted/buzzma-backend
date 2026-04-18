package com.mobo.identity.service.impl;

import com.mobo.identity.api.InvitesRequestDto;
import com.mobo.identity.api.InvitesResponseDto;
import com.mobo.identity.mapper.InvitesMapper;
import com.mobo.identity.persistence.InvitesEntity;
import com.mobo.identity.persistence.InvitesRepository;
import com.mobo.identity.service.InviteService;
import com.mobo.shared.common.BaseCrudService;
import com.mobo.shared.common.OffsetBasedPageRequest;
import java.util.List;
import java.util.UUID;
import java.util.stream.Collectors;
import org.springframework.data.domain.Sort;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
public class InviteServiceImpl extends BaseCrudService implements InviteService {

  private final InvitesRepository repository;
  private final InvitesMapper mapper;

  public InviteServiceImpl(InvitesRepository repository, InvitesMapper mapper) {
    this.repository = repository;
    this.mapper = mapper;
  }

  @Override
  @Transactional(readOnly = true)
  public List<InvitesResponseDto> list(int limit, int offset) {
    var pageable =
        new OffsetBasedPageRequest(limit, offset, Sort.by(Sort.Direction.DESC, "createdAt"));
    return repository.findAll(pageable).stream()
        .map(mapper::toResponse)
        .collect(Collectors.toList());
  }

  @Override
  @Transactional(readOnly = true)
  public InvitesResponseDto getById(UUID id) {
    InvitesEntity entity = mustFind(repository, id, "Invites");
    return mapper.toResponse(entity);
  }

  @Override
  @Transactional
  public InvitesResponseDto create(InvitesRequestDto request) {
    InvitesEntity entity = mapper.toEntity(request);
    return mapper.toResponse(repository.save(entity));
  }

  @Override
  @Transactional
  public InvitesResponseDto update(UUID id, InvitesRequestDto request) {
    InvitesEntity entity = mustFind(repository, id, "Invites");
    mapper.update(request, entity);
    return mapper.toResponse(repository.save(entity));
  }

  @Override
  @Transactional
  public void delete(UUID id) {
    repository.deleteById(id);
  }
}
