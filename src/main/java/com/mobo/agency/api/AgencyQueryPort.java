package com.mobo.agency.api;

import com.mobo.shared.enums.AgencyStatus;
import java.util.Optional;

public interface AgencyQueryPort {
  Optional<AgencyStatus> findStatusByCode(String agencyCode);
}
