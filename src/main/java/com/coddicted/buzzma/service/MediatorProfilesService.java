package com.coddicted.buzzma.service;

import com.coddicted.buzzma.common.BaseCrudService;
import com.coddicted.buzzma.common.OffsetBasedPageRequest;
import com.coddicted.buzzma.dto.MediatorProfilesRequestDto;
import com.coddicted.buzzma.dto.MediatorProfilesResponseDto;
import com.coddicted.buzzma.entity.MediatorProfilesEntity;
import com.coddicted.buzzma.mapper.MediatorProfilesMapper;
import com.coddicted.buzzma.repository.MediatorProfilesRepository;
import java.util.List;
import java.util.UUID;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Sort;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
public class MediatorProfilesService
    extends BaseCrudService<
        MediatorProfilesEntity, MediatorProfilesRequestDto, MediatorProfilesResponseDto> {
  private final MediatorProfilesRepository repository;
  private final MediatorProfilesMapper mapper;

  public MediatorProfilesService(
      final MediatorProfilesRepository repository, final MediatorProfilesMapper mapper) {
    this.repository = repository;
    this.mapper = mapper;
  }

  @Transactional(readOnly = true)
  public List<MediatorProfilesResponseDto> list(final int limit, final int offset) {
    final Pageable pageable =
        new OffsetBasedPageRequest(limit, offset, Sort.by(Sort.Direction.DESC, "createdAt"));
    return repository.findAll(pageable).getContent().stream().map(mapper::toResponse).toList();
  }

  @Transactional(readOnly = true)
  public MediatorProfilesResponseDto getById(final UUID id) {
    final MediatorProfilesEntity entity = mustFind(repository, id, "mediator_profiles");
    return mapper.toResponse(entity);
  }

  @Transactional
  public MediatorProfilesResponseDto create(final MediatorProfilesRequestDto request) {
    final MediatorProfilesEntity entity = mapper.toEntity(request);
    final MediatorProfilesEntity saved = repository.save(entity);
    return mapper.toResponse(saved);
  }

  @Transactional
  public MediatorProfilesResponseDto update(
      final UUID id, final MediatorProfilesRequestDto request) {
    final MediatorProfilesEntity existing = mustFind(repository, id, "mediator_profiles");
    mapper.update(request, existing);
    final MediatorProfilesEntity saved = repository.save(existing);
    return mapper.toResponse(saved);
  }

  @Transactional
  public void delete(final UUID id) {
    final MediatorProfilesEntity existing = mustFind(repository, id, "mediator_profiles");
    existing.setIsDeleted(true);
    repository.save(existing);
  }
}
