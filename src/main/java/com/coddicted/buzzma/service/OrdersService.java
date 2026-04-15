package com.coddicted.buzzma.service;

import com.coddicted.buzzma.common.BaseCrudService;
import com.coddicted.buzzma.common.OffsetBasedPageRequest;
import com.coddicted.buzzma.dto.OrdersRequestDto;
import com.coddicted.buzzma.dto.OrdersResponseDto;
import com.coddicted.buzzma.entity.OrdersEntity;
import com.coddicted.buzzma.mapper.OrdersMapper;
import com.coddicted.buzzma.repository.OrdersRepository;
import java.util.List;
import java.util.UUID;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Sort;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
public class OrdersService
    extends BaseCrudService<OrdersEntity, OrdersRequestDto, OrdersResponseDto> {
  private final OrdersRepository repository;
  private final OrdersMapper mapper;

  public OrdersService(final OrdersRepository repository, final OrdersMapper mapper) {
    this.repository = repository;
    this.mapper = mapper;
  }

  @Transactional(readOnly = true)
  public List<OrdersResponseDto> list(final int limit, final int offset) {
    final Pageable pageable =
        new OffsetBasedPageRequest(limit, offset, Sort.by(Sort.Direction.DESC, "createdAt"));
    return repository.findAll(pageable).getContent().stream().map(mapper::toResponse).toList();
  }

  @Transactional(readOnly = true)
  public OrdersResponseDto getById(final UUID id) {
    final OrdersEntity entity = mustFind(repository, id, "orders");
    return mapper.toResponse(entity);
  }

  @Transactional
  public OrdersResponseDto create(final OrdersRequestDto request) {
    final OrdersEntity entity = mapper.toEntity(request);
    final OrdersEntity saved = repository.save(entity);
    return mapper.toResponse(saved);
  }

  @Transactional
  public OrdersResponseDto update(final UUID id, final OrdersRequestDto request) {
    final OrdersEntity existing = mustFind(repository, id, "orders");
    mapper.update(request, existing);
    final OrdersEntity saved = repository.save(existing);
    return mapper.toResponse(saved);
  }

  @Transactional
  public void delete(final UUID id) {
    final OrdersEntity existing = mustFind(repository, id, "orders");
    existing.setIsDeleted(true);
    repository.save(existing);
  }
}
