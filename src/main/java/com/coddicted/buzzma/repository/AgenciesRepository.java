package com.coddicted.buzzma.repository;

import com.coddicted.buzzma.entity.AgenciesEntity;
import java.util.UUID;
import org.springframework.data.jpa.repository.JpaRepository;

public interface AgenciesRepository extends JpaRepository<AgenciesEntity, UUID> {}
