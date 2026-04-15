package com.coddicted.buzzma.repository;

import com.coddicted.buzzma.entity.MediatorProfilesEntity;
import java.util.UUID;
import org.springframework.data.jpa.repository.JpaRepository;

public interface MediatorProfilesRepository extends JpaRepository<MediatorProfilesEntity, UUID> {}
