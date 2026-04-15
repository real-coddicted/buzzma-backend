package com.coddicted.buzzma.repository;

import com.coddicted.buzzma.entity.SuspensionsEntity;
import java.util.UUID;
import org.springframework.data.jpa.repository.JpaRepository;

public interface SuspensionsRepository extends JpaRepository<SuspensionsEntity, UUID> {}
