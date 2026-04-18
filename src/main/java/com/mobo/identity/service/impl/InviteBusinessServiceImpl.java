package com.mobo.identity.service.impl;

import com.mobo.identity.persistence.InvitesEntity;
import com.mobo.identity.persistence.InvitesRepository;
import com.mobo.identity.service.InviteBusinessService;
import com.mobo.shared.enums.InviteStatus;
import com.mobo.shared.exception.ApiException;
import java.time.Instant;
import java.util.UUID;
import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
public class InviteBusinessServiceImpl implements InviteBusinessService {

  private final InvitesRepository invitesRepository;

  public InviteBusinessServiceImpl(InvitesRepository invitesRepository) {
    this.invitesRepository = invitesRepository;
  }

  @Override
  @Transactional
  public InvitesEntity consumeInvite(String code, String role, UUID usedByUserId) {
    InvitesEntity invite =
        invitesRepository
            .findByCode(code)
            .orElseThrow(() -> new ApiException(HttpStatus.BAD_REQUEST, "INVALID_INVITE"));

    // Role check
    if (invite.getRole() == null || !invite.getRole().name().equals(role)) {
      throw new ApiException(HttpStatus.BAD_REQUEST, "INVITE_ROLE_MISMATCH");
    }

    // Status check
    if (invite.getStatus() != InviteStatus.active) {
      throw new ApiException(HttpStatus.BAD_REQUEST, "INVALID_INVITE");
    }

    // Expiry check
    Instant now = Instant.now();
    if (invite.getExpiresAt() != null && !now.isBefore(invite.getExpiresAt())) {
      invitesRepository
          .findByCode(code)
          .ifPresent(
              i -> {
                i.setStatus(InviteStatus.expired);
                invitesRepository.save(i);
              });
      throw new ApiException(HttpStatus.BAD_REQUEST, "INVITE_EXPIRED");
    }

    int maxUses = invite.getMaxUses() != null ? invite.getMaxUses() : 1;
    int useCount = invite.getUseCount() != null ? invite.getUseCount() : 0;
    if (useCount >= maxUses) {
      throw new ApiException(HttpStatus.BAD_REQUEST, "INVALID_INVITE");
    }

    int newUseCount = useCount + 1;
    InviteStatus newStatus = newUseCount >= maxUses ? InviteStatus.used : InviteStatus.active;

    String existingUses = invite.getUses() != null ? invite.getUses() : "[]";
    String newUseEntry = String.format("{\"usedBy\":\"%s\",\"usedAt\":\"%s\"}", usedByUserId, now);
    String newUses = appendToJsonArray(existingUses, newUseEntry);

    int updated =
        invitesRepository.consumeInvite(
            code, newUseCount, newStatus, usedByUserId, now, newUses, maxUses);
    if (updated == 0) {
      throw new ApiException(HttpStatus.BAD_REQUEST, "INVALID_INVITE");
    }

    return invitesRepository
        .findByCode(code)
        .orElseThrow(() -> new ApiException(HttpStatus.NOT_FOUND, "INVITE_NOT_FOUND"));
  }

  @Override
  @Transactional
  public InvitesEntity revokeInvite(String code, UUID revokedByUserId) {
    InvitesEntity invite =
        invitesRepository
            .findByCode(code)
            .orElseThrow(() -> new ApiException(HttpStatus.NOT_FOUND, "INVITE_NOT_FOUND"));

    if (invite.getStatus() != InviteStatus.active) {
      throw new ApiException(HttpStatus.CONFLICT, "INVITE_NOT_ACTIVE");
    }

    Instant now = Instant.now();
    int updated = invitesRepository.revokeInvite(code, revokedByUserId, now);
    if (updated == 0) {
      throw new ApiException(HttpStatus.CONFLICT, "INVITE_NOT_ACTIVE");
    }

    return invitesRepository
        .findByCode(code)
        .orElseThrow(() -> new ApiException(HttpStatus.NOT_FOUND, "INVITE_NOT_FOUND"));
  }

  private String appendToJsonArray(String jsonArray, String element) {
    String trimmed = jsonArray.trim();
    if ("[]".equals(trimmed)) {
      return "[" + element + "]";
    }
    return trimmed.substring(0, trimmed.length() - 1) + "," + element + "]";
  }
}
