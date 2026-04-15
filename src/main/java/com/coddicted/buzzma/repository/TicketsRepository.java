package com.coddicted.buzzma.repository;

import com.coddicted.buzzma.entity.TicketsEntity;
import java.util.UUID;
import org.springframework.data.jpa.repository.JpaRepository;

public interface TicketsRepository extends JpaRepository<TicketsEntity, UUID> {}
