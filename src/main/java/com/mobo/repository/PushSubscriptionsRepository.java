package com.mobo.repository;

import com.mobo.entity.PushSubscriptionsEntity;
import java.util.List;
import java.util.UUID;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;

public interface PushSubscriptionsRepository extends JpaRepository<PushSubscriptionsEntity, UUID> {

  List<PushSubscriptionsEntity> findAllByUserIdAndIsDeletedFalse(UUID userId);

  Page<PushSubscriptionsEntity> findAllByIsDeletedFalse(Pageable pageable);
}
