package com.coddicted.buzzma.service;

import com.coddicted.buzzma.common.BaseCrudService;
import com.coddicted.buzzma.common.OffsetBasedPageRequest;
import com.coddicted.buzzma.dto.OrderItemsRequestDto;
import com.coddicted.buzzma.dto.OrderItemsResponseDto;
import com.coddicted.buzzma.entity.OrderItemsEntity;
import com.coddicted.buzzma.mapper.OrderItemsMapper;
import com.coddicted.buzzma.repository.OrderItemsRepository;
import java.util.List;
import java.util.UUID;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Sort;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
public class OrderItemsService
    extends BaseCrudService<OrderItemsEntity, OrderItemsRequestDto, OrderItemsResponseDto> {
  private final OrderItemsRepository repository;
  private final OrderItemsMapper mapper;

  public OrderItemsService(final OrderItemsRepository repository, final OrderItemsMapper mapper) {
    this.repository = repository;
    this.mapper = mapper;
  }

  @Transactional(readOnly = true)
  public List<OrderItemsResponseDto> list(final int limit, final int offset) {
    final Pageable pageable =
        new OffsetBasedPageRequest(limit, offset, Sort.by(Sort.Direction.DESC, "id"));
    return repository.findAll(pageable).getContent().stream().map(mapper::toResponse).toList();
  }

  @Transactional(readOnly = true)
  public OrderItemsResponseDto getById(final UUID id) {
    final OrderItemsEntity entity = mustFind(repository, id, "order_items");
    return mapper.toResponse(entity);
  }

  @Transactional
  public OrderItemsResponseDto create(final OrderItemsRequestDto request) {
    final OrderItemsEntity entity = mapper.toEntity(request);
    final OrderItemsEntity saved = repository.save(entity);
    return mapper.toResponse(saved);
  }

  @Transactional
  public OrderItemsResponseDto update(final UUID id, final OrderItemsRequestDto request) {
    final OrderItemsEntity existing = mustFind(repository, id, "order_items");
    mapper.update(request, existing);
    final OrderItemsEntity saved = repository.save(existing);
    return mapper.toResponse(saved);
  }

  @Transactional
  public void delete(final UUID id) {
    final OrderItemsEntity existing = mustFind(repository, id, "order_items");
    existing.setIsDeleted(true);
    repository.save(existing);
  }
}
