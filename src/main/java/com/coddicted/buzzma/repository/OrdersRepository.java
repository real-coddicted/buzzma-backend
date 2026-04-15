package com.coddicted.buzzma.repository;

import com.coddicted.buzzma.entity.OrdersEntity;
import java.util.UUID;
import org.springframework.data.jpa.repository.JpaRepository;

public interface OrdersRepository extends JpaRepository<OrdersEntity, UUID> {}
