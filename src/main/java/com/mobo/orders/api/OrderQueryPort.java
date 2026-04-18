package com.mobo.orders.api;

import java.util.List;
import java.util.UUID;

public interface OrderQueryPort {

  List<OrdersResponseDto> listByBrandUserId(UUID brandUserId, int limit, int offset);

  List<OrdersResponseDto> listByUserId(UUID userId, int limit, int offset);

  List<OrdersResponseDto> listByManagerNames(List<String> managerNames, int limit, int offset);

  long count();
}
