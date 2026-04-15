package com.coddicted.buzzma.repository;

import com.coddicted.buzzma.entity.CampaignsEntity;
import java.util.UUID;
import org.springframework.data.jpa.repository.JpaRepository;

public interface CampaignsRepository extends JpaRepository<CampaignsEntity, UUID> {}
