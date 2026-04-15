package com.coddicted.buzzma.service;

import com.coddicted.buzzma.common.BaseCrudService;
import com.coddicted.buzzma.common.OffsetBasedPageRequest;
import com.coddicted.buzzma.dto.SuspensionsRequestDto;
import com.coddicted.buzzma.dto.SuspensionsResponseDto;
import com.coddicted.buzzma.entity.SuspensionsEntity;
import com.coddicted.buzzma.mapper.SuspensionsMapper;
import com.coddicted.buzzma.repository.SuspensionsRepository;
import java.util.List;
import java.util.UUID;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Sort;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
public class SuspensionsService
    extends BaseCrudService<SuspensionsEntity, SuspensionsRequestDto, SuspensionsResponseDto> {
  private final SuspensionsRepository repository;
  private final SuspensionsMapper mapper;

  public SuspensionsService(
      final SuspensionsRepository repository, final SuspensionsMapper mapper) {
    this.repository = repository;
    this.mapper = mapper;
  }

  @Transactional(readOnly = true)
  public List<SuspensionsResponseDto> list(final int limit, final int offset) {
    final Pageable pageable =
        new OffsetBasedPageRequest(limit, offset, Sort.by(Sort.Direction.DESC, "createdAt"));
    return repository.findAll(pageable).getContent().stream().map(mapper::toResponse).toList();
  }

  @Transactional(readOnly = true)
  public SuspensionsResponseDto getById(final UUID id) {
    final SuspensionsEntity entity = mustFind(repository, id, "suspensions");
    return mapper.toResponse(entity);
  }

  @Transactional
  public SuspensionsResponseDto create(final SuspensionsRequestDto request) {
    final SuspensionsEntity entity = mapper.toEntity(request);
    final SuspensionsEntity saved = repository.save(entity);
    return mapper.toResponse(saved);
  }

  @Transactional
  public SuspensionsResponseDto update(final UUID id, final SuspensionsRequestDto request) {
    final SuspensionsEntity existing = mustFind(repository, id, "suspensions");
    mapper.update(request, existing);
    final SuspensionsEntity saved = repository.save(existing);
    return mapper.toResponse(saved);
  }

  @Transactional
  public void delete(final UUID id) {
    final SuspensionsEntity existing = mustFind(repository, id, "suspensions");
    repository.delete(existing);
  }
}
