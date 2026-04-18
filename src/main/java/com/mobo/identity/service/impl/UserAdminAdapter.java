package com.mobo.identity.service.impl;

import com.mobo.identity.api.UserAdminPort;
import com.mobo.identity.persistence.UsersEntity;
import com.mobo.identity.persistence.UsersRepository;
import com.mobo.shared.enums.KycStatus;
import com.mobo.shared.enums.UserStatus;
import com.mobo.shared.exception.ApiException;
import java.util.UUID;
import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
public class UserAdminAdapter implements UserAdminPort {

  private final UsersRepository usersRepository;

  public UserAdminAdapter(UsersRepository usersRepository) {
    this.usersRepository = usersRepository;
  }

  @Override
  @Transactional
  public void setStatus(UUID userId, UserStatus status, UUID actorUserId) {
    UsersEntity user = loadActive(userId);
    user.setStatus(status);
    user.setUpdatedBy(actorUserId);
    usersRepository.save(user);
  }

  @Override
  @Transactional
  public void setKycStatus(UUID userId, KycStatus kycStatus, UUID actorUserId) {
    UsersEntity user = loadActive(userId);
    user.setKycStatus(kycStatus);
    user.setUpdatedBy(actorUserId);
    usersRepository.save(user);
  }

  @Override
  @Transactional
  public void setIsVerifiedByMediator(UUID userId, boolean verified, UUID actorUserId) {
    UsersEntity user = loadActive(userId);
    user.setIsVerifiedByMediator(verified);
    user.setUpdatedBy(actorUserId);
    usersRepository.save(user);
  }

  @Override
  @Transactional
  public void markDeleted(UUID userId, UUID actorUserId) {
    UsersEntity user = loadActive(userId);
    user.setIsDeleted(true);
    user.setUpdatedBy(actorUserId);
    usersRepository.save(user);
  }

  @Override
  @Transactional
  public void setConnectedAgencies(UUID userId, String[] connectedAgencies, UUID actorUserId) {
    UsersEntity user = loadActive(userId);
    user.setConnectedAgencies(connectedAgencies);
    user.setUpdatedBy(actorUserId);
    usersRepository.save(user);
  }

  private UsersEntity loadActive(UUID userId) {
    return usersRepository
        .findById(userId)
        .filter(u -> !Boolean.TRUE.equals(u.getIsDeleted()))
        .orElseThrow(() -> new ApiException(HttpStatus.NOT_FOUND, "USER_NOT_FOUND"));
  }
}
