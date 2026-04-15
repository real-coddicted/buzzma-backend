package com.coddicted.buzzma.repository;

import com.coddicted.buzzma.entity.PayoutsEntity;
import java.util.UUID;
import org.springframework.data.jpa.repository.JpaRepository;

public interface PayoutsRepository extends JpaRepository<PayoutsEntity, UUID> {}
