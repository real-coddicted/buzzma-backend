package com.coddicted.buzzma.repository;

import com.coddicted.buzzma.entity.OrderItemsEntity;
import java.util.UUID;
import org.springframework.data.jpa.repository.JpaRepository;

public interface OrderItemsRepository extends JpaRepository<OrderItemsEntity, UUID> {}
