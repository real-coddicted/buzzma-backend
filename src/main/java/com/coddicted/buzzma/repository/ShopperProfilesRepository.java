package com.coddicted.buzzma.repository;

import com.coddicted.buzzma.entity.ShopperProfilesEntity;
import java.util.UUID;
import org.springframework.data.jpa.repository.JpaRepository;

public interface ShopperProfilesRepository extends JpaRepository<ShopperProfilesEntity, UUID> {}
