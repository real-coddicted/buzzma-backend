package com.mobo.identity.api;

import com.mobo.shared.enums.KycStatus;
import com.mobo.shared.enums.UserStatus;
import java.util.UUID;

public interface UserAdminPort {

  void setStatus(UUID userId, UserStatus status, UUID actorUserId);

  void setKycStatus(UUID userId, KycStatus kycStatus, UUID actorUserId);

  void setIsVerifiedByMediator(UUID userId, boolean verified, UUID actorUserId);

  void markDeleted(UUID userId, UUID actorUserId);

  void setConnectedAgencies(UUID userId, String[] connectedAgencies, UUID actorUserId);
}
