package com.coddicted.buzzma.repository;

import com.coddicted.buzzma.entity.AuditLogsEntity;
import java.util.UUID;
import org.springframework.data.jpa.repository.JpaRepository;

public interface AuditLogsRepository extends JpaRepository<AuditLogsEntity, UUID> {}
