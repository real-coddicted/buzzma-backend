package com.mobo.repository;

import com.mobo.entity.TicketsEntity;
import com.mobo.entity.enums.TicketStatus;
import java.util.UUID;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;

public interface TicketsRepository extends JpaRepository<TicketsEntity, UUID> {

  Page<TicketsEntity> findAllByIsDeletedFalse(Pageable pageable);

  Page<TicketsEntity> findAllByUserIdAndIsDeletedFalse(UUID userId, Pageable pageable);

  boolean existsByOrderIdAndStatusAndIsDeletedFalse(String orderId, TicketStatus status);
}
