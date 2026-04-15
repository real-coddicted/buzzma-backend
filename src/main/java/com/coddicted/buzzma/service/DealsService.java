package com.coddicted.buzzma.service;

import com.coddicted.buzzma.common.BaseCrudService;
import com.coddicted.buzzma.common.OffsetBasedPageRequest;
import com.coddicted.buzzma.dto.DealsRequestDto;
import com.coddicted.buzzma.dto.DealsResponseDto;
import com.coddicted.buzzma.entity.DealsEntity;
import com.coddicted.buzzma.mapper.DealsMapper;
import com.coddicted.buzzma.repository.DealsRepository;
import java.util.List;
import java.util.UUID;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Sort;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
public class DealsService extends BaseCrudService<DealsEntity, DealsRequestDto, DealsResponseDto> {
  private final DealsRepository repository;
  private final DealsMapper mapper;

  public DealsService(final DealsRepository repository, final DealsMapper mapper) {
    this.repository = repository;
    this.mapper = mapper;
  }

  @Transactional(readOnly = true)
  public List<DealsResponseDto> list(final int limit, final int offset) {
    final Pageable pageable =
        new OffsetBasedPageRequest(limit, offset, Sort.by(Sort.Direction.DESC, "createdAt"));
    return repository.findAll(pageable).getContent().stream().map(mapper::toResponse).toList();
  }

  @Transactional(readOnly = true)
  public DealsResponseDto getById(final UUID id) {
    final DealsEntity entity = mustFind(repository, id, "deals");
    return mapper.toResponse(entity);
  }

  @Transactional
  public DealsResponseDto create(final DealsRequestDto request) {
    final DealsEntity entity = mapper.toEntity(request);
    final DealsEntity saved = repository.save(entity);
    return mapper.toResponse(saved);
  }

  @Transactional
  public DealsResponseDto update(final UUID id, final DealsRequestDto request) {
    final DealsEntity existing = mustFind(repository, id, "deals");
    mapper.update(request, existing);
    final DealsEntity saved = repository.save(existing);
    return mapper.toResponse(saved);
  }

  @Transactional
  public void delete(final UUID id) {
    final DealsEntity existing = mustFind(repository, id, "deals");
    existing.setIsDeleted(true);
    repository.save(existing);
  }
}
