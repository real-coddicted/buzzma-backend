package com.coddicted.buzzma.service;

import com.coddicted.buzzma.common.BaseCrudService;
import com.coddicted.buzzma.common.OffsetBasedPageRequest;
import com.coddicted.buzzma.dto.SystemConfigsRequestDto;
import com.coddicted.buzzma.dto.SystemConfigsResponseDto;
import com.coddicted.buzzma.entity.SystemConfigsEntity;
import com.coddicted.buzzma.mapper.SystemConfigsMapper;
import com.coddicted.buzzma.repository.SystemConfigsRepository;
import java.util.List;
import java.util.UUID;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Sort;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
public class SystemConfigsService
    extends BaseCrudService<
        SystemConfigsEntity, SystemConfigsRequestDto, SystemConfigsResponseDto> {
  private final SystemConfigsRepository repository;
  private final SystemConfigsMapper mapper;

  public SystemConfigsService(
      final SystemConfigsRepository repository, final SystemConfigsMapper mapper) {
    this.repository = repository;
    this.mapper = mapper;
  }

  @Transactional(readOnly = true)
  public List<SystemConfigsResponseDto> list(final int limit, final int offset) {
    final Pageable pageable =
        new OffsetBasedPageRequest(limit, offset, Sort.by(Sort.Direction.DESC, "createdAt"));
    return repository.findAll(pageable).getContent().stream().map(mapper::toResponse).toList();
  }

  @Transactional(readOnly = true)
  public SystemConfigsResponseDto getById(final UUID id) {
    final SystemConfigsEntity entity = mustFind(repository, id, "system_configs");
    return mapper.toResponse(entity);
  }

  @Transactional
  public SystemConfigsResponseDto create(final SystemConfigsRequestDto request) {
    final SystemConfigsEntity entity = mapper.toEntity(request);
    final SystemConfigsEntity saved = repository.save(entity);
    return mapper.toResponse(saved);
  }

  @Transactional
  public SystemConfigsResponseDto update(final UUID id, final SystemConfigsRequestDto request) {
    final SystemConfigsEntity existing = mustFind(repository, id, "system_configs");
    mapper.update(request, existing);
    final SystemConfigsEntity saved = repository.save(existing);
    return mapper.toResponse(saved);
  }

  @Transactional
  public void delete(final UUID id) {
    final SystemConfigsEntity existing = mustFind(repository, id, "system_configs");
    repository.delete(existing);
  }
}
