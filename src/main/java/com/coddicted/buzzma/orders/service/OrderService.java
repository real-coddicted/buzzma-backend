package com.coddicted.buzzma.orders.service;

import com.coddicted.buzzma.orders.api.OrdersRequestDto;
import com.coddicted.buzzma.orders.api.OrdersResponseDto;
import java.util.List;
import java.util.UUID;

public interface OrderService {

  List<OrdersResponseDto> list(int limit, int offset);

  OrdersResponseDto getById(UUID id);

  OrdersResponseDto create(OrdersRequestDto request);

  OrdersResponseDto update(UUID id, OrdersRequestDto request);

  void delete(UUID id);
}
