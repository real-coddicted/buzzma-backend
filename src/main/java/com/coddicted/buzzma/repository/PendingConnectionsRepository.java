package com.coddicted.buzzma.repository;

import com.coddicted.buzzma.entity.PendingConnectionsEntity;
import java.util.UUID;
import org.springframework.data.jpa.repository.JpaRepository;

public interface PendingConnectionsRepository
    extends JpaRepository<PendingConnectionsEntity, UUID> {}
