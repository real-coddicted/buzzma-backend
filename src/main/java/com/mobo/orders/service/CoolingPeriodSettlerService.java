package com.mobo.orders.service;

import com.mobo.orders.persistence.OrdersEntity;
import java.util.List;

public interface CoolingPeriodSettlerService {

  List<OrdersEntity> runSettlement();
}
