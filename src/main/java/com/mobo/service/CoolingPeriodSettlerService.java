package com.mobo.service;

import com.mobo.entity.OrdersEntity;
import java.util.List;

public interface CoolingPeriodSettlerService {

  List<OrdersEntity> runSettlement();
}
