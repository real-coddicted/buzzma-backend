package com.mobo.repository;

import com.mobo.entity.AgenciesEntity;
import java.util.Optional;
import java.util.UUID;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;

public interface AgenciesRepository extends JpaRepository<AgenciesEntity, UUID> {

  Page<AgenciesEntity> findAllByIsDeletedFalse(Pageable pageable);

  Optional<AgenciesEntity> findByAgencyCodeAndIsDeletedFalse(String agencyCode);
}
