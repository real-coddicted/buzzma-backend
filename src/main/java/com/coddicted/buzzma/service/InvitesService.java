package com.coddicted.buzzma.service;

import com.coddicted.buzzma.common.BaseCrudService;
import com.coddicted.buzzma.common.OffsetBasedPageRequest;
import com.coddicted.buzzma.dto.InvitesRequestDto;
import com.coddicted.buzzma.dto.InvitesResponseDto;
import com.coddicted.buzzma.entity.InvitesEntity;
import com.coddicted.buzzma.mapper.InvitesMapper;
import com.coddicted.buzzma.repository.InvitesRepository;
import java.util.List;
import java.util.UUID;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Sort;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
public class InvitesService
    extends BaseCrudService<InvitesEntity, InvitesRequestDto, InvitesResponseDto> {
  private final InvitesRepository repository;
  private final InvitesMapper mapper;

  public InvitesService(final InvitesRepository repository, final InvitesMapper mapper) {
    this.repository = repository;
    this.mapper = mapper;
  }

  @Transactional(readOnly = true)
  public List<InvitesResponseDto> list(final int limit, final int offset) {
    final Pageable pageable =
        new OffsetBasedPageRequest(limit, offset, Sort.by(Sort.Direction.DESC, "createdAt"));
    return repository.findAll(pageable).getContent().stream().map(mapper::toResponse).toList();
  }

  @Transactional(readOnly = true)
  public InvitesResponseDto getById(final UUID id) {
    final InvitesEntity entity = mustFind(repository, id, "invites");
    return mapper.toResponse(entity);
  }

  @Transactional
  public InvitesResponseDto create(final InvitesRequestDto request) {
    final InvitesEntity entity = mapper.toEntity(request);
    final InvitesEntity saved = repository.save(entity);
    return mapper.toResponse(saved);
  }

  @Transactional
  public InvitesResponseDto update(final UUID id, final InvitesRequestDto request) {
    final InvitesEntity existing = mustFind(repository, id, "invites");
    mapper.update(request, existing);
    final InvitesEntity saved = repository.save(existing);
    return mapper.toResponse(saved);
  }

  @Transactional
  public void delete(final UUID id) {
    final InvitesEntity existing = mustFind(repository, id, "invites");
    repository.delete(existing);
  }
}
