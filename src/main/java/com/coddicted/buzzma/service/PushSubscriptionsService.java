package com.coddicted.buzzma.service;

import com.coddicted.buzzma.common.BaseCrudService;
import com.coddicted.buzzma.common.OffsetBasedPageRequest;
import com.coddicted.buzzma.dto.PushSubscriptionsRequestDto;
import com.coddicted.buzzma.dto.PushSubscriptionsResponseDto;
import com.coddicted.buzzma.entity.PushSubscriptionsEntity;
import com.coddicted.buzzma.mapper.PushSubscriptionsMapper;
import com.coddicted.buzzma.repository.PushSubscriptionsRepository;
import java.util.List;
import java.util.UUID;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Sort;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
public class PushSubscriptionsService
    extends BaseCrudService<
        PushSubscriptionsEntity, PushSubscriptionsRequestDto, PushSubscriptionsResponseDto> {
  private final PushSubscriptionsRepository repository;
  private final PushSubscriptionsMapper mapper;

  public PushSubscriptionsService(
      final PushSubscriptionsRepository repository, final PushSubscriptionsMapper mapper) {
    this.repository = repository;
    this.mapper = mapper;
  }

  @Transactional(readOnly = true)
  public List<PushSubscriptionsResponseDto> list(final int limit, final int offset) {
    final Pageable pageable =
        new OffsetBasedPageRequest(limit, offset, Sort.by(Sort.Direction.DESC, "createdAt"));
    return repository.findAll(pageable).getContent().stream().map(mapper::toResponse).toList();
  }

  @Transactional(readOnly = true)
  public PushSubscriptionsResponseDto getById(final UUID id) {
    final PushSubscriptionsEntity entity = mustFind(repository, id, "push_subscriptions");
    return mapper.toResponse(entity);
  }

  @Transactional
  public PushSubscriptionsResponseDto create(final PushSubscriptionsRequestDto request) {
    final PushSubscriptionsEntity entity = mapper.toEntity(request);
    final PushSubscriptionsEntity saved = repository.save(entity);
    return mapper.toResponse(saved);
  }

  @Transactional
  public PushSubscriptionsResponseDto update(
      final UUID id, final PushSubscriptionsRequestDto request) {
    final PushSubscriptionsEntity existing = mustFind(repository, id, "push_subscriptions");
    mapper.update(request, existing);
    final PushSubscriptionsEntity saved = repository.save(existing);
    return mapper.toResponse(saved);
  }

  @Transactional
  public void delete(final UUID id) {
    final PushSubscriptionsEntity existing = mustFind(repository, id, "push_subscriptions");
    existing.setIsDeleted(true);
    repository.save(existing);
  }
}
