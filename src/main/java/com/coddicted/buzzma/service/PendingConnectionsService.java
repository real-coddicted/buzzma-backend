package com.coddicted.buzzma.service;

import com.coddicted.buzzma.common.BaseCrudService;
import com.coddicted.buzzma.common.OffsetBasedPageRequest;
import com.coddicted.buzzma.dto.PendingConnectionsRequestDto;
import com.coddicted.buzzma.dto.PendingConnectionsResponseDto;
import com.coddicted.buzzma.entity.PendingConnectionsEntity;
import com.coddicted.buzzma.mapper.PendingConnectionsMapper;
import com.coddicted.buzzma.repository.PendingConnectionsRepository;
import java.util.List;
import java.util.UUID;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Sort;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
public class PendingConnectionsService
    extends BaseCrudService<
        PendingConnectionsEntity, PendingConnectionsRequestDto, PendingConnectionsResponseDto> {
  private final PendingConnectionsRepository repository;
  private final PendingConnectionsMapper mapper;

  public PendingConnectionsService(
      final PendingConnectionsRepository repository, final PendingConnectionsMapper mapper) {
    this.repository = repository;
    this.mapper = mapper;
  }

  @Transactional(readOnly = true)
  public List<PendingConnectionsResponseDto> list(final int limit, final int offset) {
    final Pageable pageable =
        new OffsetBasedPageRequest(limit, offset, Sort.by(Sort.Direction.DESC, "id"));
    return repository.findAll(pageable).getContent().stream().map(mapper::toResponse).toList();
  }

  @Transactional(readOnly = true)
  public PendingConnectionsResponseDto getById(final UUID id) {
    final PendingConnectionsEntity entity = mustFind(repository, id, "pending_connections");
    return mapper.toResponse(entity);
  }

  @Transactional
  public PendingConnectionsResponseDto create(final PendingConnectionsRequestDto request) {
    final PendingConnectionsEntity entity = mapper.toEntity(request);
    final PendingConnectionsEntity saved = repository.save(entity);
    return mapper.toResponse(saved);
  }

  @Transactional
  public PendingConnectionsResponseDto update(
      final UUID id, final PendingConnectionsRequestDto request) {
    final PendingConnectionsEntity existing = mustFind(repository, id, "pending_connections");
    mapper.update(request, existing);
    final PendingConnectionsEntity saved = repository.save(existing);
    return mapper.toResponse(saved);
  }

  @Transactional
  public void delete(final UUID id) {
    final PendingConnectionsEntity existing = mustFind(repository, id, "pending_connections");
    existing.setIsDeleted(true);
    repository.save(existing);
  }
}
