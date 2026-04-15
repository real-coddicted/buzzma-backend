package com.coddicted.buzzma.repository;

import com.coddicted.buzzma.entity.PushSubscriptionsEntity;
import java.util.UUID;
import org.springframework.data.jpa.repository.JpaRepository;

public interface PushSubscriptionsRepository extends JpaRepository<PushSubscriptionsEntity, UUID> {}
