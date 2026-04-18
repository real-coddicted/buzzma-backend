package com.mobo.identity.service.impl;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.when;

import com.mobo.identity.persistence.InvitesEntity;
import com.mobo.identity.persistence.InvitesRepository;
import com.mobo.shared.enums.InviteStatus;
import com.mobo.shared.enums.UserRole;
import com.mobo.shared.exception.ApiException;
import java.time.Instant;
import java.util.Optional;
import java.util.UUID;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.http.HttpStatus;

@ExtendWith(MockitoExtension.class)
class InviteBusinessServiceImplTest {

  @Mock private InvitesRepository invitesRepository;
  @InjectMocks private InviteBusinessServiceImpl service;

  // ── consumeInvite ───────────────────────────────────────────────────────

  @Test
  void consumeInvite_notFound_throwsBadRequest() {
    when(invitesRepository.findByCode("MISSING")).thenReturn(Optional.empty());

    assertThatThrownBy(() -> service.consumeInvite("MISSING", "shopper", UUID.randomUUID()))
        .isInstanceOf(ApiException.class)
        .satisfies(
            ex -> assertThat(((ApiException) ex).getStatus()).isEqualTo(HttpStatus.BAD_REQUEST));
  }

  @Test
  void consumeInvite_roleMismatch_throwsBadRequest() {
    InvitesEntity invite = activeInvite("CODE1", UserRole.mediator, 10, 1);
    when(invitesRepository.findByCode("CODE1")).thenReturn(Optional.of(invite));

    assertThatThrownBy(() -> service.consumeInvite("CODE1", "shopper", UUID.randomUUID()))
        .isInstanceOf(ApiException.class)
        .satisfies(
            ex -> assertThat(((ApiException) ex).getCode()).isEqualTo("INVITE_ROLE_MISMATCH"));
  }

  @Test
  void consumeInvite_expired_throwsBadRequest() {
    InvitesEntity invite = activeInvite("CODE2", UserRole.shopper, 10, 0);
    invite.setExpiresAt(Instant.now().minusSeconds(3600)); // expired
    when(invitesRepository.findByCode("CODE2")).thenReturn(Optional.of(invite));

    assertThatThrownBy(() -> service.consumeInvite("CODE2", "shopper", UUID.randomUUID()))
        .isInstanceOf(ApiException.class)
        .satisfies(ex -> assertThat(((ApiException) ex).getCode()).isEqualTo("INVITE_EXPIRED"));
  }

  @Test
  void consumeInvite_exhausted_throwsBadRequest() {
    InvitesEntity invite = activeInvite("CODE3", UserRole.shopper, 1, 1); // useCount == maxUses
    when(invitesRepository.findByCode("CODE3")).thenReturn(Optional.of(invite));

    assertThatThrownBy(() -> service.consumeInvite("CODE3", "shopper", UUID.randomUUID()))
        .isInstanceOf(ApiException.class)
        .satisfies(
            ex -> assertThat(((ApiException) ex).getStatus()).isEqualTo(HttpStatus.BAD_REQUEST));
  }

  @Test
  void consumeInvite_success_returnsInvite() {
    UUID userId = UUID.randomUUID();
    InvitesEntity invite = activeInvite("CODE4", UserRole.shopper, 10, 0);
    InvitesEntity consumed = activeInvite("CODE4", UserRole.shopper, 10, 1);

    when(invitesRepository.findByCode("CODE4"))
        .thenReturn(Optional.of(invite))
        .thenReturn(Optional.of(consumed));
    when(invitesRepository.consumeInvite(
            eq("CODE4"), eq(1), eq(InviteStatus.active), eq(userId), any(), any(), eq(10)))
        .thenReturn(1);

    InvitesEntity result = service.consumeInvite("CODE4", "shopper", userId);

    assertThat(result.getUseCount()).isEqualTo(1);
  }

  @Test
  void consumeInvite_atomicRaceLost_throwsBadRequest() {
    UUID userId = UUID.randomUUID();
    InvitesEntity invite = activeInvite("CODE5", UserRole.shopper, 10, 0);

    when(invitesRepository.findByCode("CODE5")).thenReturn(Optional.of(invite));
    when(invitesRepository.consumeInvite(
            eq("CODE5"), eq(1), eq(InviteStatus.active), eq(userId), any(), any(), eq(10)))
        .thenReturn(0); // race lost

    assertThatThrownBy(() -> service.consumeInvite("CODE5", "shopper", userId))
        .isInstanceOf(ApiException.class)
        .satisfies(
            ex -> assertThat(((ApiException) ex).getStatus()).isEqualTo(HttpStatus.BAD_REQUEST));
  }

  // ── revokeInvite ────────────────────────────────────────────────────────

  @Test
  void revokeInvite_notActive_throwsConflict() {
    UUID actorId = UUID.randomUUID();
    InvitesEntity invite = activeInvite("REV1", UserRole.shopper, 5, 5);
    invite.setStatus(InviteStatus.used);
    when(invitesRepository.findByCode("REV1")).thenReturn(Optional.of(invite));

    assertThatThrownBy(() -> service.revokeInvite("REV1", actorId))
        .isInstanceOf(ApiException.class)
        .satisfies(ex -> assertThat(((ApiException) ex).getCode()).isEqualTo("INVITE_NOT_ACTIVE"));
  }

  @Test
  void revokeInvite_success_returnsRevoked() {
    UUID actorId = UUID.randomUUID();
    InvitesEntity invite = activeInvite("REV2", UserRole.mediator, 5, 0);
    InvitesEntity revoked = activeInvite("REV2", UserRole.mediator, 5, 0);
    revoked.setStatus(InviteStatus.revoked);

    when(invitesRepository.findByCode("REV2"))
        .thenReturn(Optional.of(invite))
        .thenReturn(Optional.of(revoked));
    when(invitesRepository.revokeInvite(eq("REV2"), eq(actorId), any())).thenReturn(1);

    InvitesEntity result = service.revokeInvite("REV2", actorId);

    assertThat(result.getStatus()).isEqualTo(InviteStatus.revoked);
  }

  // ── helpers ─────────────────────────────────────────────────────────────

  private InvitesEntity activeInvite(String code, UserRole role, int maxUses, int useCount) {
    InvitesEntity invite = new InvitesEntity();
    invite.setCode(code);
    invite.setRole(role);
    invite.setStatus(InviteStatus.active);
    invite.setMaxUses(maxUses);
    invite.setUseCount(useCount);
    return invite;
  }
}
