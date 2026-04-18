package com.coddicted.buzzma.brands.service;

import com.coddicted.buzzma.orders.api.OrdersResponseDto;
import com.coddicted.buzzma.wallet.api.TransactionsResponseDto;
import java.util.List;
import java.util.UUID;

public interface BrandDomainService {

  List<OrdersResponseDto> getBrandOrders(UUID brandUserId, int limit, int offset);

  List<TransactionsResponseDto> getLedger(UUID brandUserId, int limit, int offset);

  void connectAgency(UUID brandUserId, String agencyCode);
}
