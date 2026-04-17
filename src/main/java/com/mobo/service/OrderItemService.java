package com.mobo.service;

import com.mobo.dto.OrderItemsRequestDto;
import com.mobo.dto.OrderItemsResponseDto;
import java.util.List;
import java.util.UUID;

public interface OrderItemService {

  List<OrderItemsResponseDto> list(int limit, int offset);

  OrderItemsResponseDto getById(UUID id);

  OrderItemsResponseDto create(OrderItemsRequestDto request);

  OrderItemsResponseDto update(UUID id, OrderItemsRequestDto request);

  void delete(UUID id);
}
