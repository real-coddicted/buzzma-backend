package com.coddicted.buzzma.repository;

import com.coddicted.buzzma.entity.TicketCommentsEntity;
import java.util.UUID;
import org.springframework.data.jpa.repository.JpaRepository;

public interface TicketCommentsRepository extends JpaRepository<TicketCommentsEntity, UUID> {}
