package com.coddicted.buzzma.repository;

import com.coddicted.buzzma.entity.BrandsEntity;
import java.util.UUID;
import org.springframework.data.jpa.repository.JpaRepository;

public interface BrandsRepository extends JpaRepository<BrandsEntity, UUID> {}
