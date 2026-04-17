package com.mobo.service;

import com.mobo.dto.OrdersResponseDto;
import com.mobo.dto.TransactionsResponseDto;
import java.util.List;
import java.util.UUID;

public interface BrandDomainService {

  List<OrdersResponseDto> getBrandOrders(UUID brandUserId, int limit, int offset);

  List<TransactionsResponseDto> getLedger(UUID brandUserId, int limit, int offset);

  void connectAgency(UUID brandUserId, String agencyCode);
}
