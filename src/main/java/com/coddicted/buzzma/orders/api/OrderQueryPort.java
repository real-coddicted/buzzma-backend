package com.coddicted.buzzma.orders.api;

import java.time.Instant;
import java.util.List;
import java.util.Map;
import java.util.UUID;

public interface OrderQueryPort {

  List<OrdersResponseDto> listByBrandUserId(UUID brandUserId, int limit, int offset);

  List<OrdersResponseDto> listByUserId(UUID userId, int limit, int offset);

  List<OrdersResponseDto> listByManagerNames(List<String> managerNames, int limit, int offset);

  long count();

  long sumTotalPaiseByManagerNames(List<String> managerNames);

  long countByManagerNamesAndCreatedAtAfter(List<String> managerNames, Instant since);

  List<Map<String, Object>> findDailyRevenue(List<String> managerNames, Instant start, Instant end);

  List<Map<String, Object>> findTopBrandsByOrderCount(List<String> managerNames);
}
