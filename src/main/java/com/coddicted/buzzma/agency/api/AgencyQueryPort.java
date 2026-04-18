package com.coddicted.buzzma.agency.api;

import com.coddicted.buzzma.shared.enums.AgencyStatus;
import java.util.Optional;

public interface AgencyQueryPort {
  Optional<AgencyStatus> findStatusByCode(String agencyCode);
}
