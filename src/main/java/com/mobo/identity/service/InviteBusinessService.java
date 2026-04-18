package com.mobo.identity.service;

import com.mobo.identity.persistence.InvitesEntity;
import java.util.UUID;

public interface InviteBusinessService {

  InvitesEntity consumeInvite(String code, String role, UUID usedByUserId);

  InvitesEntity revokeInvite(String code, UUID revokedByUserId);
}
