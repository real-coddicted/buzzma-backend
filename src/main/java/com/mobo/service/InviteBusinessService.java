package com.mobo.service;

import com.mobo.entity.InvitesEntity;
import java.util.UUID;

public interface InviteBusinessService {

  InvitesEntity consumeInvite(String code, String role, UUID usedByUserId);

  InvitesEntity revokeInvite(String code, UUID revokedByUserId);
}
