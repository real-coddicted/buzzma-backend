package com.coddicted.buzzma.orders.service.impl;

import com.coddicted.buzzma.orders.api.OrderItemsRequestDto;
import com.coddicted.buzzma.orders.api.OrderItemsResponseDto;
import com.coddicted.buzzma.orders.mapper.OrderItemsMapper;
import com.coddicted.buzzma.orders.persistence.OrderItemsEntity;
import com.coddicted.buzzma.orders.persistence.OrderItemsRepository;
import com.coddicted.buzzma.orders.service.OrderItemService;
import com.coddicted.buzzma.shared.common.BaseCrudService;
import com.coddicted.buzzma.shared.common.OffsetBasedPageRequest;
import java.util.List;
import java.util.UUID;
import java.util.stream.Collectors;
import org.springframework.data.domain.Sort;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
public class OrderItemServiceImpl extends BaseCrudService implements OrderItemService {

  private final OrderItemsRepository repository;
  private final OrderItemsMapper mapper;

  public OrderItemServiceImpl(OrderItemsRepository repository, OrderItemsMapper mapper) {
    this.repository = repository;
    this.mapper = mapper;
  }

  @Override
  @Transactional(readOnly = true)
  public List<OrderItemsResponseDto> list(int limit, int offset) {
    var pageable =
        new OffsetBasedPageRequest(limit, offset, Sort.by(Sort.Direction.DESC, "createdAt"));
    return repository.findAllByIsDeletedFalse(pageable).stream()
        .map(mapper::toResponse)
        .collect(Collectors.toList());
  }

  @Override
  @Transactional(readOnly = true)
  public OrderItemsResponseDto getById(UUID id) {
    OrderItemsEntity entity = mustFind(repository, id, "OrderItems");
    return mapper.toResponse(entity);
  }

  @Override
  @Transactional
  public OrderItemsResponseDto create(OrderItemsRequestDto request) {
    OrderItemsEntity entity = mapper.toEntity(request);
    return mapper.toResponse(repository.save(entity));
  }

  @Override
  @Transactional
  public OrderItemsResponseDto update(UUID id, OrderItemsRequestDto request) {
    OrderItemsEntity entity = mustFind(repository, id, "OrderItems");
    mapper.update(request, entity);
    return mapper.toResponse(repository.save(entity));
  }

  @Override
  @Transactional
  public void delete(UUID id) {
    OrderItemsEntity entity = mustFind(repository, id, "OrderItems");
    entity.setIsDeleted(true);
    repository.save(entity);
  }
}
