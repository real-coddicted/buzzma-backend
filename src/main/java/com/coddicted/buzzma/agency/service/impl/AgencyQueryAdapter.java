package com.coddicted.buzzma.agency.service.impl;

import com.coddicted.buzzma.agency.api.AgencyQueryPort;
import com.coddicted.buzzma.agency.persistence.AgenciesRepository;
import com.coddicted.buzzma.shared.enums.AgencyStatus;
import java.util.Optional;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
public class AgencyQueryAdapter implements AgencyQueryPort {

  private final AgenciesRepository agenciesRepository;

  public AgencyQueryAdapter(AgenciesRepository agenciesRepository) {
    this.agenciesRepository = agenciesRepository;
  }

  @Override
  @Transactional(readOnly = true)
  public Optional<AgencyStatus> findStatusByCode(String agencyCode) {
    return agenciesRepository.findByAgencyCodeAndIsDeletedFalse(agencyCode).map(a -> a.getStatus());
  }
}
