package com.mobo.repository;

import com.mobo.entity.CampaignsEntity;
import java.util.UUID;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;

public interface CampaignsRepository extends JpaRepository<CampaignsEntity, UUID> {

  Page<CampaignsEntity> findAllByIsDeletedFalse(Pageable pageable);

  Page<CampaignsEntity> findAllByBrandUserIdAndIsDeletedFalse(UUID brandUserId, Pageable pageable);
}
