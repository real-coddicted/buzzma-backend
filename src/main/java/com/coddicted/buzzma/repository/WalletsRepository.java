package com.coddicted.buzzma.repository;

import com.coddicted.buzzma.entity.WalletsEntity;
import java.util.UUID;
import org.springframework.data.jpa.repository.JpaRepository;

public interface WalletsRepository extends JpaRepository<WalletsEntity, UUID> {}
