package com.coddicted.buzzma.catalog.persistence;

import java.util.UUID;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

public interface CampaignsRepository extends JpaRepository<CampaignsEntity, UUID> {

  Page<CampaignsEntity> findAllByIsDeletedFalse(Pageable pageable);

  Page<CampaignsEntity> findAllByBrandUserIdAndIsDeletedFalse(UUID brandUserId, Pageable pageable);

  @Query(
      value =
          "SELECT COUNT(DISTINCT c.id) FROM campaigns c"
              + " WHERE c.is_deleted = false AND c.status = 'active'"
              + " AND (c.allowed_agency_codes @> ARRAY[:agencyCode]::text[]"
              + "   OR EXISTS ("
              + "     SELECT 1 FROM jsonb_object_keys(c.assignments) k"
              + "     WHERE k = ANY(:managerCodes)"
              + "   ))",
      nativeQuery = true)
  long countActiveCampaignsForAgency(
      @Param("agencyCode") String agencyCode, @Param("managerCodes") String[] managerCodes);
}
