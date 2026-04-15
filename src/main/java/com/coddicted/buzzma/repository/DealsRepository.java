package com.coddicted.buzzma.repository;

import com.coddicted.buzzma.entity.DealsEntity;
import java.util.UUID;
import org.springframework.data.jpa.repository.JpaRepository;

public interface DealsRepository extends JpaRepository<DealsEntity, UUID> {}
