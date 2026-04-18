package com.mobo.mediator.service.impl;

import com.mobo.mediator.api.MediatorAdminPort;
import com.mobo.mediator.persistence.PendingConnectionsEntity;
import com.mobo.mediator.persistence.PendingConnectionsRepository;
import com.mobo.shared.exception.ApiException;
import java.time.Instant;
import java.util.UUID;
import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
public class MediatorAdminAdapter implements MediatorAdminPort {

  private final PendingConnectionsRepository pendingConnectionsRepository;

  public MediatorAdminAdapter(PendingConnectionsRepository pendingConnectionsRepository) {
    this.pendingConnectionsRepository = pendingConnectionsRepository;
  }

  @Override
  @Transactional
  public void requestBrandConnection(
      UUID brandUserId, String brandCode, String agencyCode, String agencyName, UUID actorUserId) {

    pendingConnectionsRepository
        .findByUserIdAndAgencyCodeAndIsDeletedFalse(brandUserId, agencyCode)
        .ifPresent(
            p -> {
              throw new ApiException(HttpStatus.CONFLICT, "ALREADY_REQUESTED");
            });

    int pendingCount = pendingConnectionsRepository.countByUserIdAndIsDeletedFalse(brandUserId);
    if (pendingCount >= 100) {
      throw new ApiException(HttpStatus.CONFLICT, "TOO_MANY_PENDING");
    }

    PendingConnectionsEntity conn = new PendingConnectionsEntity();
    conn.setUserId(brandUserId);
    conn.setAgencyId(actorUserId.toString());
    conn.setAgencyName(agencyName);
    conn.setAgencyCode(agencyCode);
    conn.setTimestamp(Instant.now());
    pendingConnectionsRepository.save(conn);
  }
}
