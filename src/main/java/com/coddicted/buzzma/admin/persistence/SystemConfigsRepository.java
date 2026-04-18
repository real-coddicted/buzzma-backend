package com.coddicted.buzzma.admin.persistence;

import java.util.Optional;
import java.util.UUID;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;

public interface SystemConfigsRepository extends JpaRepository<SystemConfigsEntity, UUID> {

  Optional<SystemConfigsEntity> findByKey(String key);

  Page<SystemConfigsEntity> findAll(Pageable pageable);
}
