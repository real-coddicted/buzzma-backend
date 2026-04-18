package com.mobo.admin.persistence;

import java.util.UUID;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;

public interface SuspensionsRepository extends JpaRepository<SuspensionsEntity, UUID> {

  Page<SuspensionsEntity> findAllByTargetUserId(UUID targetUserId, Pageable pageable);
}
