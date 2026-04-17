package com.mobo.repository;

import com.mobo.entity.PendingConnectionsEntity;
import java.util.List;
import java.util.UUID;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;

public interface PendingConnectionsRepository
    extends JpaRepository<PendingConnectionsEntity, UUID> {

  Page<PendingConnectionsEntity> findAllByIsDeletedFalse(Pageable pageable);

  List<PendingConnectionsEntity> findAllByUserIdAndIsDeletedFalse(UUID userId);

  java.util.Optional<PendingConnectionsEntity> findByUserIdAndAgencyCodeAndIsDeletedFalse(
      UUID userId, String agencyCode);

  int countByUserIdAndIsDeletedFalse(UUID userId);
}
