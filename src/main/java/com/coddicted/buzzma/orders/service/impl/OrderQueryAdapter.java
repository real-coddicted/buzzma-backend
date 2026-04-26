package com.coddicted.buzzma.orders.service.impl;

import com.coddicted.buzzma.orders.api.OrderQueryPort;
import com.coddicted.buzzma.orders.api.OrdersResponseDto;
import com.coddicted.buzzma.orders.mapper.OrdersMapper;
import com.coddicted.buzzma.orders.persistence.OrdersRepository;
import com.coddicted.buzzma.shared.common.OffsetBasedPageRequest;
import java.time.Instant;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
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

  @Override
  @Transactional(readOnly = true)
  public long sumTotalPaiseByManagerNames(List<String> managerNames) {
    if (managerNames == null || managerNames.isEmpty()) {
      return 0L;
    }
    return ordersRepository.sumTotalPaiseByManagerNames(managerNames.toArray(new String[0]));
  }

  @Override
  @Transactional(readOnly = true)
  public long countByManagerNamesAndCreatedAtAfter(List<String> managerNames, Instant since) {
    if (managerNames == null || managerNames.isEmpty()) {
      return 0L;
    }
    return ordersRepository.countByManagerNamesAndCreatedAtAfter(
        managerNames.toArray(new String[0]), since);
  }

  @Override
  @Transactional(readOnly = true)
  public List<Map<String, Object>> findDailyRevenue(
      List<String> managerNames, Instant start, Instant end) {
    if (managerNames == null || managerNames.isEmpty()) {
      return List.of();
    }
    return ordersRepository
        .findDailyRevenue(managerNames.toArray(new String[0]), start, end)
        .stream()
        .map(
            row -> {
              Map<String, Object> m = new HashMap<>();
              m.put("day", row[0]);
              m.put("total", row[1]);
              return m;
            })
        .collect(Collectors.toList());
  }

  @Override
  @Transactional(readOnly = true)
  public List<Map<String, Object>> findTopBrandsByOrderCount(List<String> managerNames) {
    if (managerNames == null || managerNames.isEmpty()) {
      return List.of();
    }
    return ordersRepository.findTopBrandsByOrderCount(managerNames.toArray(new String[0])).stream()
        .map(
            row -> {
              Map<String, Object> m = new HashMap<>();
              m.put("name", row[0]);
              m.put("count", row[1]);
              return m;
            })
        .collect(Collectors.toList());
  }
}
