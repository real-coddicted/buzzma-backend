package com.coddicted.buzzma.catalog.service.impl;

import com.coddicted.buzzma.catalog.api.DealsRequestDto;
import com.coddicted.buzzma.catalog.api.DealsResponseDto;
import com.coddicted.buzzma.catalog.mapper.DealsMapper;
import com.coddicted.buzzma.catalog.persistence.DealsEntity;
import com.coddicted.buzzma.catalog.persistence.DealsRepository;
import com.coddicted.buzzma.catalog.service.DealService;
import com.coddicted.buzzma.shared.common.BaseCrudService;
import com.coddicted.buzzma.shared.common.OffsetBasedPageRequest;
import java.util.List;
import java.util.UUID;
import java.util.stream.Collectors;
import org.springframework.data.domain.Sort;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
public class DealServiceImpl extends BaseCrudService implements DealService {

  private final DealsRepository repository;
  private final DealsMapper mapper;

  public DealServiceImpl(DealsRepository repository, DealsMapper mapper) {
    this.repository = repository;
    this.mapper = mapper;
  }

  @Override
  @Transactional(readOnly = true)
  public List<DealsResponseDto> list(int limit, int offset) {
    var pageable =
        new OffsetBasedPageRequest(limit, offset, Sort.by(Sort.Direction.DESC, "createdAt"));
    return repository.findAllByIsDeletedFalse(pageable).stream()
        .map(mapper::toResponse)
        .collect(Collectors.toList());
  }

  @Override
  @Transactional(readOnly = true)
  public DealsResponseDto getById(UUID id) {
    DealsEntity entity = mustFind(repository, id, "Deals");
    return mapper.toResponse(entity);
  }

  @Override
  @Transactional
  public DealsResponseDto create(DealsRequestDto request) {
    DealsEntity entity = mapper.toEntity(request);
    return mapper.toResponse(repository.save(entity));
  }

  @Override
  @Transactional
  public DealsResponseDto update(UUID id, DealsRequestDto request) {
    DealsEntity entity = mustFind(repository, id, "Deals");
    mapper.update(request, entity);
    return mapper.toResponse(repository.save(entity));
  }

  @Override
  @Transactional
  public void delete(UUID id) {
    DealsEntity entity = mustFind(repository, id, "Deals");
    entity.setIsDeleted(true);
    repository.save(entity);
  }
}
