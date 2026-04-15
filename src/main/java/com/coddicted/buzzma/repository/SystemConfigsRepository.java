package com.coddicted.buzzma.repository;

import com.coddicted.buzzma.entity.SystemConfigsEntity;
import java.util.UUID;
import org.springframework.data.jpa.repository.JpaRepository;

public interface SystemConfigsRepository extends JpaRepository<SystemConfigsEntity, UUID> {}
