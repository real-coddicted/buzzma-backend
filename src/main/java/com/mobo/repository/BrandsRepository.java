package com.mobo.repository;

import com.mobo.entity.BrandsEntity;
import java.util.Optional;
import java.util.UUID;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;

public interface BrandsRepository extends JpaRepository<BrandsEntity, UUID> {

  Page<BrandsEntity> findAllByIsDeletedFalse(Pageable pageable);

  Optional<BrandsEntity> findByBrandCodeAndIsDeletedFalse(String brandCode);
}
