package com.coddicted.buzzma.orders.service;

import com.coddicted.buzzma.orders.persistence.OrdersEntity;
import java.util.List;

public interface CoolingPeriodSettlerService {

  List<OrdersEntity> runSettlement();
}
