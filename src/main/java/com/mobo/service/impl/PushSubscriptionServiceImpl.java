package com.mobo.service.impl;

import com.mobo.common.BaseCrudService;
import com.mobo.common.OffsetBasedPageRequest;
import com.mobo.dto.PushSubscriptionsRequestDto;
import com.mobo.dto.PushSubscriptionsResponseDto;
import com.mobo.entity.PushSubscriptionsEntity;
import com.mobo.mapper.PushSubscriptionsMapper;
import com.mobo.repository.PushSubscriptionsRepository;
import com.mobo.service.PushSubscriptionService;
import java.util.List;
import java.util.UUID;
import java.util.stream.Collectors;
import org.springframework.data.domain.Sort;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
public class PushSubscriptionServiceImpl extends BaseCrudService
    implements PushSubscriptionService {

  private final PushSubscriptionsRepository repository;
  private final PushSubscriptionsMapper mapper;

  public PushSubscriptionServiceImpl(
      PushSubscriptionsRepository repository, PushSubscriptionsMapper mapper) {
    this.repository = repository;
    this.mapper = mapper;
  }

  @Override
  @Transactional(readOnly = true)
  public List<PushSubscriptionsResponseDto> list(int limit, int offset) {
    var pageable =
        new OffsetBasedPageRequest(limit, offset, Sort.by(Sort.Direction.DESC, "createdAt"));
    return repository.findAllByIsDeletedFalse(pageable).stream()
        .map(mapper::toResponse)
        .collect(Collectors.toList());
  }

  @Override
  @Transactional(readOnly = true)
  public PushSubscriptionsResponseDto getById(UUID id) {
    PushSubscriptionsEntity entity = mustFind(repository, id, "PushSubscriptions");
    return mapper.toResponse(entity);
  }

  @Override
  @Transactional
  public PushSubscriptionsResponseDto create(PushSubscriptionsRequestDto request) {
    PushSubscriptionsEntity entity = mapper.toEntity(request);
    return mapper.toResponse(repository.save(entity));
  }

  @Override
  @Transactional
  public PushSubscriptionsResponseDto update(UUID id, PushSubscriptionsRequestDto request) {
    PushSubscriptionsEntity entity = mustFind(repository, id, "PushSubscriptions");
    mapper.update(request, entity);
    return mapper.toResponse(repository.save(entity));
  }

  @Override
  @Transactional
  public void delete(UUID id) {
    PushSubscriptionsEntity entity = mustFind(repository, id, "PushSubscriptions");
    entity.setIsDeleted(true);
    repository.save(entity);
  }
}
