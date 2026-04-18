package com.mobo.orders.service.impl;

import com.mobo.orders.api.OrderQueryPort;
import com.mobo.orders.api.OrdersResponseDto;
import com.mobo.orders.mapper.OrdersMapper;
import com.mobo.orders.persistence.OrdersRepository;
import com.mobo.shared.common.OffsetBasedPageRequest;
import java.util.List;
import java.util.UUID;
import java.util.stream.Collectors;
import org.springframework.data.domain.Sort;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
public class OrderQueryAdapter implements OrderQueryPort {

  private final OrdersRepository ordersRepository;
  private final OrdersMapper ordersMapper;

  public OrderQueryAdapter(OrdersRepository ordersRepository, OrdersMapper ordersMapper) {
    this.ordersRepository = ordersRepository;
    this.ordersMapper = ordersMapper;
  }

  @Override
  @Transactional(readOnly = true)
  public List<OrdersResponseDto> listByBrandUserId(UUID brandUserId, int limit, int offset) {
    var pageable =
        new OffsetBasedPageRequest(limit, offset, Sort.by(Sort.Direction.DESC, "createdAt"));
    return ordersRepository.findAllByBrandUserIdAndIsDeletedFalse(brandUserId, pageable).stream()
        .map(ordersMapper::toResponse)
        .collect(Collectors.toList());
  }

  @Override
  @Transactional(readOnly = true)
  public List<OrdersResponseDto> listByUserId(UUID userId, int limit, int offset) {
    var pageable =
        new OffsetBasedPageRequest(limit, offset, Sort.by(Sort.Direction.DESC, "createdAt"));
    return ordersRepository.findAllByUserIdAndIsDeletedFalse(userId, pageable).stream()
        .map(ordersMapper::toResponse)
        .collect(Collectors.toList());
  }

  @Override
  @Transactional(readOnly = true)
  public List<OrdersResponseDto> listByManagerNames(
      List<String> managerNames, int limit, int offset) {
    if (managerNames == null || managerNames.isEmpty()) {
      return List.of();
    }
    var pageable =
        new OffsetBasedPageRequest(limit, offset, Sort.by(Sort.Direction.DESC, "createdAt"));
    return ordersRepository.findAllByManagerNameInAndIsDeletedFalse(managerNames, pageable).stream()
        .map(ordersMapper::toResponse)
        .collect(Collectors.toList());
  }

  @Override
  @Transactional(readOnly = true)
  public long count() {
    return ordersRepository.count();
  }
}
