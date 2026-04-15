package com.coddicted.buzzma.service;

import com.coddicted.buzzma.common.BaseCrudService;
import com.coddicted.buzzma.common.OffsetBasedPageRequest;
import com.coddicted.buzzma.dto.AgenciesRequestDto;
import com.coddicted.buzzma.dto.AgenciesResponseDto;
import com.coddicted.buzzma.entity.AgenciesEntity;
import com.coddicted.buzzma.mapper.AgenciesMapper;
import com.coddicted.buzzma.repository.AgenciesRepository;
import java.util.List;
import java.util.UUID;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Sort;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
public class AgenciesService
    extends BaseCrudService<AgenciesEntity, AgenciesRequestDto, AgenciesResponseDto> {
  private final AgenciesRepository repository;
  private final AgenciesMapper mapper;

  public AgenciesService(final AgenciesRepository repository, final AgenciesMapper mapper) {
    this.repository = repository;
    this.mapper = mapper;
  }

  @Transactional(readOnly = true)
  public List<AgenciesResponseDto> list(final int limit, final int offset) {
    final Pageable pageable =
        new OffsetBasedPageRequest(limit, offset, Sort.by(Sort.Direction.DESC, "createdAt"));
    return repository.findAll(pageable).getContent().stream().map(mapper::toResponse).toList();
  }

  @Transactional(readOnly = true)
  public AgenciesResponseDto getById(final UUID id) {
    final AgenciesEntity entity = mustFind(repository, id, "agencies");
    return mapper.toResponse(entity);
  }

  @Transactional
  public AgenciesResponseDto create(final AgenciesRequestDto request) {
    final AgenciesEntity entity = mapper.toEntity(request);
    final AgenciesEntity saved = repository.save(entity);
    return mapper.toResponse(saved);
  }

  @Transactional
  public AgenciesResponseDto update(final UUID id, final AgenciesRequestDto request) {
    final AgenciesEntity existing = mustFind(repository, id, "agencies");
    mapper.update(request, existing);
    final AgenciesEntity saved = repository.save(existing);
    return mapper.toResponse(saved);
  }

  @Transactional
  public void delete(final UUID id) {
    final AgenciesEntity existing = mustFind(repository, id, "agencies");
    existing.setIsDeleted(true);
    repository.save(existing);
  }
}
