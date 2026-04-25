package com.coddicted.buzzma.identity.service.impl;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.when;

import com.coddicted.buzzma.identity.api.auth.LoginRequest;
import com.coddicted.buzzma.identity.api.auth.LoginResponse;
import com.coddicted.buzzma.identity.api.auth.RegisterRequest;
import com.coddicted.buzzma.identity.persistence.InvitesEntity;
import com.coddicted.buzzma.identity.persistence.SecurityQuestionsRepository;
import com.coddicted.buzzma.identity.persistence.UsersEntity;
import com.coddicted.buzzma.identity.persistence.UsersRepository;
import com.coddicted.buzzma.identity.service.InviteBusinessService;
import com.coddicted.buzzma.shared.common.PasswordService;
import com.coddicted.buzzma.shared.enums.UserRole;
import com.coddicted.buzzma.shared.enums.UserStatus;
import com.coddicted.buzzma.shared.exception.ApiException;
import com.coddicted.buzzma.shared.security.JwtService;
import com.coddicted.buzzma.wallet.api.WalletBusinessService;
import com.coddicted.buzzma.wallet.api.WalletsResponseDto;
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
class AuthServiceImplTest {

  @Mock private UsersRepository usersRepository;
  @Mock private SecurityQuestionsRepository securityQuestionsRepository;
  @Mock private PasswordService passwordService;
  @Mock private JwtService jwtService;
  @Mock private WalletBusinessService walletBusinessService;
  @Mock private InviteBusinessService inviteBusinessService;
  @InjectMocks private AuthServiceImpl service;

  // ── register ────────────────────────────────────────────────────────────

  @Test
  void register_mobileTaken_throwsConflict() {
    RegisterRequest req =
        RegisterRequest.builder()
            .name("Alice")
            .mobile("9999999999")
            .password("password123")
            .mediatorCode("MED01")
            .build();

    UsersEntity existing = new UsersEntity();
    when(usersRepository.findByMobileAndIsDeletedFalse("9999999999"))
        .thenReturn(Optional.of(existing));

    assertThatThrownBy(() -> service.register(req))
        .isInstanceOf(ApiException.class)
        .satisfies(
            ex -> assertThat(((ApiException) ex).getStatus()).isEqualTo(HttpStatus.CONFLICT));
  }

  @Test
  void register_invalidInviteAndNoParent_throwsBadRequest() {
    RegisterRequest req =
        RegisterRequest.builder()
            .name("Bob")
            .mobile("8888888888")
            .password("password123")
            .mediatorCode("BADCODE")
            .build();

    when(usersRepository.findByMobileAndIsDeletedFalse("8888888888")).thenReturn(Optional.empty());
    when(inviteBusinessService.consumeInvite(any(), any(), any()))
        .thenThrow(new ApiException(HttpStatus.BAD_REQUEST, "INVALID_INVITE"));
    // fallback: upstream mediator code "BADCODE" is also invalid
    when(usersRepository.findByMediatorCodeAndIsDeletedFalse("BADCODE"))
        .thenReturn(Optional.empty());

    assertThatThrownBy(() -> service.register(req))
        .isInstanceOf(ApiException.class)
        .satisfies(
            ex -> assertThat(((ApiException) ex).getStatus()).isEqualTo(HttpStatus.BAD_REQUEST));
  }

  @Test
  void register_success_returnsLoginResponse() {
    RegisterRequest req =
        RegisterRequest.builder()
            .name("Carol")
            .mobile("7777777777")
            .password("password123")
            .mediatorCode("MED02")
            .build();

    InvitesEntity invite = new InvitesEntity();
    invite.setParentCode("MED02");

    UsersEntity parent = new UsersEntity();
    parent.setStatus(UserStatus.active);

    UsersEntity saved = new UsersEntity();
    saved.setId(UUID.randomUUID());
    saved.setRole(UserRole.shopper);
    saved.setRoles(new String[] {"shopper"});
    saved.setStatus(UserStatus.active);
    saved.setName("Carol");

    WalletsResponseDto wallet =
        WalletsResponseDto.builder().availablePaise(0).pendingPaise(0).build();

    when(usersRepository.findByMobileAndIsDeletedFalse("7777777777")).thenReturn(Optional.empty());
    when(inviteBusinessService.consumeInvite("MED02", "shopper", null)).thenReturn(invite);
    when(usersRepository.findByMediatorCodeAndIsDeletedFalse("MED02"))
        .thenReturn(Optional.of(parent));
    when(passwordService.hashPassword("password123")).thenReturn("$hash$");
    when(usersRepository.save(any())).thenReturn(saved);
    when(walletBusinessService.ensureWallet(saved.getId())).thenReturn(wallet);
    when(jwtService.generateAccessToken(saved.getId())).thenReturn("access-token");
    when(jwtService.generateRefreshToken(saved.getId())).thenReturn("refresh-token");

    LoginResponse response = service.register(req);

    assertThat(response.getTokens().getAccessToken()).isEqualTo("access-token");
    assertThat(response.getTokens().getRefreshToken()).isEqualTo("refresh-token");
    assertThat(response.getUser()).isNotNull();
  }

  // ── login ───────────────────────────────────────────────────────────────

  @Test
  void login_userNotFound_throwsUnauthorized() {
    LoginRequest req = LoginRequest.builder().mobile("0000000000").password("pass1234").build();
    when(usersRepository.findByMobileAndIsDeletedFalse("0000000000")).thenReturn(Optional.empty());
    when(passwordService.verifyPassword(any(), any())).thenReturn(false);

    assertThatThrownBy(() -> service.login(req))
        .isInstanceOf(ApiException.class)
        .satisfies(
            ex -> assertThat(((ApiException) ex).getStatus()).isEqualTo(HttpStatus.UNAUTHORIZED));
  }

  @Test
  void login_wrongPassword_throwsUnauthorized() {
    LoginRequest req = LoginRequest.builder().mobile("1111111111").password("wrongpass").build();
    UsersEntity user = activeUser("1111111111", UserRole.shopper);
    user.setPasswordHash("$hash$");

    when(usersRepository.findByMobileAndIsDeletedFalse("1111111111")).thenReturn(Optional.of(user));
    when(passwordService.verifyPassword("wrongpass", "$hash$")).thenReturn(false);
    when(usersRepository.save(any())).thenReturn(user);

    assertThatThrownBy(() -> service.login(req))
        .isInstanceOf(ApiException.class)
        .satisfies(
            ex -> assertThat(((ApiException) ex).getStatus()).isEqualTo(HttpStatus.UNAUTHORIZED));
  }

  @Test
  void login_suspendedUser_throwsForbidden() {
    LoginRequest req = LoginRequest.builder().mobile("2222222222").password("pass1234").build();
    UsersEntity user = new UsersEntity();
    user.setMobile("2222222222");
    user.setRole(UserRole.shopper);
    user.setStatus(UserStatus.suspended);

    when(usersRepository.findByMobileAndIsDeletedFalse("2222222222")).thenReturn(Optional.of(user));

    assertThatThrownBy(() -> service.login(req))
        .isInstanceOf(ApiException.class)
        .satisfies(
            ex -> assertThat(((ApiException) ex).getStatus()).isEqualTo(HttpStatus.FORBIDDEN));
  }

  @Test
  void login_lockedAccount_throwsTooManyRequests() {
    LoginRequest req = LoginRequest.builder().mobile("3333333333").password("pass1234").build();
    UsersEntity user = activeUser("3333333333", UserRole.shopper);
    user.setLockoutUntil(Instant.now().plusSeconds(600)); // locked for 10 min

    when(usersRepository.findByMobileAndIsDeletedFalse("3333333333")).thenReturn(Optional.of(user));

    assertThatThrownBy(() -> service.login(req))
        .isInstanceOf(ApiException.class)
        .satisfies(
            ex ->
                assertThat(((ApiException) ex).getStatus())
                    .isEqualTo(HttpStatus.TOO_MANY_REQUESTS));
  }

  @Test
  void login_success_returnsTokens() {
    LoginRequest req = LoginRequest.builder().mobile("4444444444").password("pass1234").build();
    UsersEntity user = activeUser("4444444444", UserRole.shopper);
    user.setId(UUID.randomUUID());
    user.setPasswordHash("$hash$");
    WalletsResponseDto wallet =
        WalletsResponseDto.builder().availablePaise(500).pendingPaise(0).build();

    when(usersRepository.findByMobileAndIsDeletedFalse("4444444444")).thenReturn(Optional.of(user));
    when(passwordService.verifyPassword("pass1234", "$hash$")).thenReturn(true);
    when(walletBusinessService.ensureWallet(user.getId())).thenReturn(wallet);
    when(jwtService.generateAccessToken(user.getId())).thenReturn("at");
    when(jwtService.generateRefreshToken(user.getId())).thenReturn("rt");

    LoginResponse response = service.login(req);

    assertThat(response.getTokens().getAccessToken()).isEqualTo("at");
    assertThat(response.getTokens().getRefreshToken()).isEqualTo("rt");
  }

  // ── refresh ─────────────────────────────────────────────────────────────

  @Test
  void refresh_validToken_returnsNewTokens() {
    UUID userId = UUID.randomUUID();
    UsersEntity user = activeUser("5555555555", UserRole.shopper);
    user.setId(userId);
    WalletsResponseDto wallet =
        WalletsResponseDto.builder().availablePaise(0).pendingPaise(0).build();

    when(jwtService.validateRefreshToken("rt-valid")).thenReturn(userId);
    when(usersRepository.findById(userId)).thenReturn(Optional.of(user));
    when(walletBusinessService.ensureWallet(userId)).thenReturn(wallet);
    when(jwtService.generateAccessToken(userId)).thenReturn("new-at");
    when(jwtService.generateRefreshToken(userId)).thenReturn("new-rt");

    LoginResponse response = service.refresh("rt-valid");

    assertThat(response.getTokens().getAccessToken()).isEqualTo("new-at");
  }

  @Test
  void refresh_userDeleted_throwsUnauthorized() {
    UUID userId = UUID.randomUUID();
    UsersEntity user = activeUser("6666666666", UserRole.shopper);
    user.setIsDeleted(true);

    when(jwtService.validateRefreshToken("rt-deleted")).thenReturn(userId);
    when(usersRepository.findById(userId)).thenReturn(Optional.of(user));

    assertThatThrownBy(() -> service.refresh("rt-deleted"))
        .isInstanceOf(ApiException.class)
        .satisfies(
            ex -> assertThat(((ApiException) ex).getStatus()).isEqualTo(HttpStatus.UNAUTHORIZED));
  }

  // ── helpers ─────────────────────────────────────────────────────────────

  private UsersEntity activeUser(String mobile, UserRole role) {
    UsersEntity user = new UsersEntity();
    user.setId(UUID.randomUUID());
    user.setMobile(mobile);
    user.setRole(role);
    user.setRoles(new String[] {role.name()});
    user.setStatus(UserStatus.active);
    user.setIsDeleted(false);
    return user;
  }
}
