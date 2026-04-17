package com.mobo.repository;

import com.mobo.entity.TicketCommentsEntity;
import java.util.UUID;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;

public interface TicketCommentsRepository extends JpaRepository<TicketCommentsEntity, UUID> {

  Page<TicketCommentsEntity> findAllByTicketIdAndIsDeletedFalse(UUID ticketId, Pageable pageable);
}
