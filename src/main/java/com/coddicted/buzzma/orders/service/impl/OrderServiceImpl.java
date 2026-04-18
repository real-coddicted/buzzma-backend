package com.coddicted.buzzma.orders.service.impl;

import com.coddicted.buzzma.orders.api.OrdersRequestDto;
import com.coddicted.buzzma.orders.api.OrdersResponseDto;
import com.coddicted.buzzma.orders.mapper.OrdersMapper;
import com.coddicted.buzzma.orders.persistence.OrdersEntity;
import com.coddicted.buzzma.orders.persistence.OrdersRepository;
import com.coddicted.buzzma.orders.service.OrderService;
import com.coddicted.buzzma.shared.common.BaseCrudService;
import com.coddicted.buzzma.shared.common.OffsetBasedPageRequest;
import java.util.List;
import java.util.UUID;
import java.util.stream.Collectors;
import org.springframework.data.domain.Sort;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
public class OrderServiceImpl extends BaseCrudService implements OrderService {

  private final OrdersRepository repository;
  private final OrdersMapper mapper;

  public OrderServiceImpl(OrdersRepository repository, OrdersMapper mapper) {
    this.repository = repository;
    this.mapper = mapper;
  }

  @Override
  @Transactional(readOnly = true)
  public List<OrdersResponseDto> list(int limit, int offset) {
    var pageable =
        new OffsetBasedPageRequest(limit, offset, Sort.by(Sort.Direction.DESC, "createdAt"));
    return repository.findAllByIsDeletedFalse(pageable).stream()
        .map(mapper::toResponse)
        .collect(Collectors.toList());
  }

  @Override
  @Transactional(readOnly = true)
  public OrdersResponseDto getById(UUID id) {
    OrdersEntity entity = mustFind(repository, id, "Orders");
    return mapper.toResponse(entity);
  }

  @Override
  @Transactional
  public OrdersResponseDto create(OrdersRequestDto request) {
    OrdersEntity entity = mapper.toEntity(request);
    return mapper.toResponse(repository.save(entity));
  }

  @Override
  @Transactional
  public OrdersResponseDto update(UUID id, OrdersRequestDto request) {
    OrdersEntity entity = mustFind(repository, id, "Orders");
    mapper.update(request, entity);
    return mapper.toResponse(repository.save(entity));
  }

  @Override
  @Transactional
  public void delete(UUID id) {
    OrdersEntity entity = mustFind(repository, id, "Orders");
    entity.setIsDeleted(true);
    repository.save(entity);
  }
}
