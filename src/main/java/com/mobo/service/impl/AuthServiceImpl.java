package com.mobo.service.impl;

import com.mobo.common.PasswordService;
import com.mobo.dto.auth.ForgotPasswordLookupRequest;
import com.mobo.dto.auth.ForgotPasswordResetRequest;
import com.mobo.dto.auth.LoginRequest;
import com.mobo.dto.auth.LoginResponse;
import com.mobo.dto.auth.RegisterBrandRequest;
import com.mobo.dto.auth.RegisterOpsRequest;
import com.mobo.dto.auth.RegisterRequest;
import com.mobo.dto.auth.SecurityQuestionsRequest;
import com.mobo.dto.auth.UpdateProfileRequest;
import com.mobo.dto.auth.UserSummary;
import com.mobo.entity.InvitesEntity;
import com.mobo.entity.SecurityQuestionsEntity;
import com.mobo.entity.UsersEntity;
import com.mobo.entity.WalletsEntity;
import com.mobo.entity.enums.UserRole;
import com.mobo.entity.enums.UserStatus;
import com.mobo.exception.ApiException;
import com.mobo.repository.SecurityQuestionsRepository;
import com.mobo.repository.UsersRepository;
import com.mobo.security.JwtService;
import com.mobo.service.AuthService;
import com.mobo.service.InviteBusinessService;
import com.mobo.service.WalletBusinessService;
import java.time.Instant;
import java.util.List;
import java.util.UUID;
import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
public class AuthServiceImpl implements AuthService {

  private static final int MAX_FAILED_ATTEMPTS = 7;
  private static final long LOCKOUT_DURATION_MS = 15 * 60 * 1000L;

  private final UsersRepository usersRepository;
  private final SecurityQuestionsRepository securityQuestionsRepository;
  private final PasswordService passwordService;
  private final JwtService jwtService;
  private final WalletBusinessService walletBusinessService;
  private final InviteBusinessService inviteBusinessService;

  public AuthServiceImpl(
      UsersRepository usersRepository,
      SecurityQuestionsRepository securityQuestionsRepository,
      PasswordService passwordService,
      JwtService jwtService,
      WalletBusinessService walletBusinessService,
      InviteBusinessService inviteBusinessService) {
    this.usersRepository = usersRepository;
    this.securityQuestionsRepository = securityQuestionsRepository;
    this.passwordService = passwordService;
    this.jwtService = jwtService;
    this.walletBusinessService = walletBusinessService;
    this.inviteBusinessService = inviteBusinessService;
  }

  @Override
  @Transactional
  public LoginResponse register(RegisterRequest request) {
    // Check mobile uniqueness
    usersRepository
        .findByMobileAndIsDeletedFalse(request.getMobile())
        .ifPresent(
            u -> {
              throw new ApiException(HttpStatus.CONFLICT, "MOBILE_ALREADY_EXISTS");
            });

    String passwordHash = passwordService.hashPassword(request.getPassword());

    // Look up invite or validate mediator code
    String upstreamMediatorCode;
    try {
      InvitesEntity consumed =
          inviteBusinessService.consumeInvite(request.getMediatorCode(), "shopper", null);
      upstreamMediatorCode = consumed.getParentCode() != null ? consumed.getParentCode() : "";
    } catch (ApiException e) {
      // Fallback: treat mediatorCode as direct upstream mediator code
      upstreamMediatorCode = request.getMediatorCode();
    }

    if (upstreamMediatorCode.isBlank()) {
      throw new ApiException(HttpStatus.BAD_REQUEST, "INVALID_INVITE");
    }

    // Validate upstream mediator is active
    usersRepository
        .findByMediatorCodeAndIsDeletedFalse(upstreamMediatorCode)
        .filter(u -> u.getStatus() == UserStatus.active)
        .orElseThrow(() -> new ApiException(HttpStatus.BAD_REQUEST, "INVALID_INVITE_PARENT"));

    // Create user
    UsersEntity user = new UsersEntity();
    user.setName(request.getName());
    user.setMobile(request.getMobile());
    user.setEmail(request.getEmail());
    user.setPasswordHash(passwordHash);
    user.setRole(UserRole.shopper);
    user.setRoles(new String[] {"shopper"});
    user.setStatus(UserStatus.active);
    user.setParentCode(upstreamMediatorCode);
    user.setIsVerifiedByMediator(false);
    user = usersRepository.save(user);

    // Retry invite consume with the newly created userId
    try {
      inviteBusinessService.consumeInvite(request.getMediatorCode(), "shopper", user.getId());
    } catch (ApiException ignored) {
      // Invite may already be consumed above or may not be invite-based
    }

    WalletsEntity wallet = walletBusinessService.ensureWallet(user.getId());

    return buildLoginResponse(user, wallet);
  }

  @Override
  @Transactional
  public LoginResponse login(LoginRequest request) {
    String mobile = request.getMobile() != null ? request.getMobile().trim() : null;
    String username =
        request.getUsername() != null ? request.getUsername().trim().toLowerCase() : null;

    UsersEntity user =
        (mobile != null && !mobile.isBlank())
            ? usersRepository.findByMobileAndIsDeletedFalse(mobile).orElse(null)
            : (username != null
                ? usersRepository.findByUsernameAndIsDeletedFalse(username).orElse(null)
                : null);

    // Constant-time dummy compare if user not found
    if (user == null) {
      passwordService.verifyPassword(
          request.getPassword(), "$2a$12$DUMMY_HASH_TO_PREVENT_TIMING_ATTACK_00000000000");
      throw new ApiException(HttpStatus.UNAUTHORIZED, "INVALID_CREDENTIALS");
    }

    // Admin/ops cannot login via mobile
    if (mobile != null && !mobile.isBlank()) {
      String role = user.getRole() != null ? user.getRole().name() : "";
      if ("admin".equals(role) || "ops".equals(role)) {
        throw new ApiException(HttpStatus.BAD_REQUEST, "USERNAME_REQUIRED");
      }
    }

    if (user.getStatus() != UserStatus.active) {
      throw new ApiException(HttpStatus.FORBIDDEN, "USER_NOT_ACTIVE");
    }

    // Lockout check
    if (user.getLockoutUntil() != null && user.getLockoutUntil().isAfter(Instant.now())) {
      long minutesLeft =
          (user.getLockoutUntil().toEpochMilli() - Instant.now().toEpochMilli()) / 60_000 + 1;
      throw new ApiException(
          HttpStatus.TOO_MANY_REQUESTS,
          "ACCOUNT_LOCKED",
          "Account locked. Try again in " + minutesLeft + " minute(s).");
    }

    // Verify password
    boolean ok =
        user.getPasswordHash() != null
            && passwordService.verifyPassword(request.getPassword(), user.getPasswordHash());
    if (!ok) {
      int newAttempts =
          (user.getFailedLoginAttempts() != null ? user.getFailedLoginAttempts() : 0) + 1;
      user.setFailedLoginAttempts(newAttempts);
      if (newAttempts >= MAX_FAILED_ATTEMPTS) {
        user.setLockoutUntil(Instant.now().plusMillis(LOCKOUT_DURATION_MS));
      }
      usersRepository.save(user);
      throw new ApiException(HttpStatus.UNAUTHORIZED, "INVALID_CREDENTIALS");
    }

    // Reset failed attempts on successful login
    if (user.getFailedLoginAttempts() != null && user.getFailedLoginAttempts() > 0) {
      user.setFailedLoginAttempts(0);
      user.setLockoutUntil(null);
      usersRepository.save(user);
    }

    WalletsEntity wallet = walletBusinessService.ensureWallet(user.getId());
    return buildLoginResponse(user, wallet);
  }

  @Override
  public LoginResponse refresh(String refreshToken) {
    UUID userId = jwtService.validateRefreshToken(refreshToken);
    UsersEntity user =
        usersRepository
            .findById(userId)
            .filter(u -> !Boolean.TRUE.equals(u.getIsDeleted()))
            .filter(u -> u.getStatus() == UserStatus.active)
            .orElseThrow(() -> new ApiException(HttpStatus.UNAUTHORIZED, "UNAUTHENTICATED"));

    WalletsEntity wallet = walletBusinessService.ensureWallet(userId);
    return buildLoginResponse(user, wallet);
  }

  @Override
  public UserSummary me(UUID userId) {
    UsersEntity user =
        usersRepository
            .findById(userId)
            .filter(u -> !Boolean.TRUE.equals(u.getIsDeleted()))
            .orElseThrow(() -> new ApiException(HttpStatus.UNAUTHORIZED, "UNAUTHENTICATED"));

    WalletsEntity wallet = walletBusinessService.ensureWallet(userId);
    return toUserSummary(user, wallet);
  }

  @Override
  @Transactional
  public LoginResponse registerOps(RegisterOpsRequest request) {
    usersRepository
        .findByMobileAndIsDeletedFalse(request.getMobile())
        .ifPresent(
            u -> {
              throw new ApiException(HttpStatus.CONFLICT, "MOBILE_ALREADY_EXISTS");
            });

    UserRole role;
    try {
      role = UserRole.valueOf(request.getRole());
    } catch (IllegalArgumentException e) {
      throw new ApiException(HttpStatus.BAD_REQUEST, "INVALID_ROLE");
    }

    if (role != UserRole.agency && role != UserRole.mediator) {
      throw new ApiException(HttpStatus.BAD_REQUEST, "INVALID_ROLE");
    }

    // Validate invite code
    inviteBusinessService.consumeInvite(request.getCode(), role.name(), null);

    UsersEntity user = new UsersEntity();
    user.setName(request.getName());
    user.setMobile(request.getMobile());
    user.setPasswordHash(passwordService.hashPassword(request.getPassword()));
    user.setRole(role);
    user.setRoles(new String[] {role.name()});
    user.setStatus(UserStatus.active);
    user = usersRepository.save(user);

    WalletsEntity wallet = walletBusinessService.ensureWallet(user.getId());
    return buildLoginResponse(user, wallet);
  }

  @Override
  @Transactional
  public LoginResponse registerBrand(RegisterBrandRequest request) {
    usersRepository
        .findByMobileAndIsDeletedFalse(request.getMobile())
        .ifPresent(
            u -> {
              throw new ApiException(HttpStatus.CONFLICT, "MOBILE_ALREADY_EXISTS");
            });

    UsersEntity user = new UsersEntity();
    user.setName(request.getName());
    user.setMobile(request.getMobile());
    user.setPasswordHash(passwordService.hashPassword(request.getPassword()));
    user.setRole(UserRole.brand);
    user.setRoles(new String[] {"brand"});
    user.setBrandCode(request.getBrandCode());
    user.setStatus(UserStatus.active);
    user = usersRepository.save(user);

    WalletsEntity wallet = walletBusinessService.ensureWallet(user.getId());
    return buildLoginResponse(user, wallet);
  }

  @Override
  @Transactional
  public UserSummary updateProfile(UUID userId, UpdateProfileRequest request) {
    UsersEntity user =
        usersRepository
            .findById(userId)
            .filter(u -> !Boolean.TRUE.equals(u.getIsDeleted()))
            .orElseThrow(() -> new ApiException(HttpStatus.NOT_FOUND, "USER_NOT_FOUND"));

    if (request.getName() != null && !request.getName().isBlank()) {
      user.setName(request.getName());
    }
    if (request.getEmail() != null) {
      user.setEmail(request.getEmail().isBlank() ? null : request.getEmail());
    }
    if (request.getAvatar() != null) {
      user.setAvatar(request.getAvatar().isBlank() ? null : request.getAvatar());
    }
    if (request.getUpiId() != null) {
      user.setUpiId(request.getUpiId().isBlank() ? null : request.getUpiId());
    }
    if (request.getQrCode() != null) {
      user.setQrCode(request.getQrCode().isBlank() ? null : request.getQrCode());
    }
    if (request.getBankAccountNumber() != null) {
      user.setBankAccountNumber(
          request.getBankAccountNumber().isBlank() ? null : request.getBankAccountNumber());
    }
    if (request.getBankIfsc() != null) {
      user.setBankIfsc(request.getBankIfsc().isBlank() ? null : request.getBankIfsc());
    }
    if (request.getBankName() != null) {
      user.setBankName(request.getBankName().isBlank() ? null : request.getBankName());
    }
    if (request.getBankHolderName() != null) {
      user.setBankHolderName(
          request.getBankHolderName().isBlank() ? null : request.getBankHolderName());
    }

    user = usersRepository.save(user);
    WalletsEntity wallet = walletBusinessService.ensureWallet(userId);
    return toUserSummary(user, wallet);
  }

  @Override
  @Transactional
  public void saveSecurityQuestions(UUID userId, SecurityQuestionsRequest request) {
    if (request.getQuestions() == null || request.getQuestions().size() != 3) {
      throw new ApiException(HttpStatus.BAD_REQUEST, "INVALID_SECURITY_QUESTIONS");
    }

    for (SecurityQuestionsRequest.QuestionAnswer qa : request.getQuestions()) {
      String answerHash = passwordService.hashPassword(qa.getAnswer().trim().toLowerCase());
      SecurityQuestionsEntity existing =
          securityQuestionsRepository
              .findByUserIdAndQuestionId(userId, qa.getQuestionId())
              .orElse(null);

      if (existing != null) {
        existing.setAnswerHash(answerHash);
        existing.setUpdatedAt(Instant.now());
        securityQuestionsRepository.save(existing);
      } else {
        SecurityQuestionsEntity sq = new SecurityQuestionsEntity();
        sq.setUserId(userId);
        sq.setQuestionId(qa.getQuestionId());
        sq.setAnswerHash(answerHash);
        securityQuestionsRepository.save(sq);
      }
    }
  }

  @Override
  public UserSummary forgotPasswordLookup(ForgotPasswordLookupRequest request) {
    UsersEntity user =
        usersRepository
            .findByMobileAndIsDeletedFalse(request.getMobile())
            .orElseThrow(() -> new ApiException(HttpStatus.NOT_FOUND, "USER_NOT_FOUND"));

    List<SecurityQuestionsEntity> questions =
        securityQuestionsRepository.findAllByUserId(user.getId());
    if (questions.isEmpty()) {
      throw new ApiException(HttpStatus.NOT_FOUND, "SECURITY_QUESTIONS_NOT_SET");
    }

    WalletsEntity wallet = walletBusinessService.ensureWallet(user.getId());
    return toUserSummary(user, wallet);
  }

  @Override
  @Transactional
  public void forgotPasswordReset(ForgotPasswordResetRequest request) {
    UsersEntity user =
        usersRepository
            .findByMobileAndIsDeletedFalse(request.getMobile())
            .orElseThrow(() -> new ApiException(HttpStatus.NOT_FOUND, "USER_NOT_FOUND"));

    List<SecurityQuestionsEntity> savedQuestions =
        securityQuestionsRepository.findAllByUserId(user.getId());
    if (savedQuestions.isEmpty()) {
      throw new ApiException(HttpStatus.BAD_REQUEST, "SECURITY_QUESTIONS_NOT_SET");
    }

    // Verify all 3 answers
    for (SecurityQuestionsRequest.QuestionAnswer qa : request.getAnswers()) {
      SecurityQuestionsEntity saved =
          savedQuestions.stream()
              .filter(q -> q.getQuestionId().equals(qa.getQuestionId()))
              .findFirst()
              .orElseThrow(
                  () -> new ApiException(HttpStatus.BAD_REQUEST, "INVALID_SECURITY_QUESTION"));

      boolean matches =
          passwordService.verifyPassword(
              qa.getAnswer().trim().toLowerCase(), saved.getAnswerHash());
      if (!matches) {
        throw new ApiException(HttpStatus.UNAUTHORIZED, "INVALID_SECURITY_ANSWER");
      }
    }

    user.setPasswordHash(passwordService.hashPassword(request.getNewPassword()));
    user.setFailedLoginAttempts(0);
    user.setLockoutUntil(null);
    usersRepository.save(user);
  }

  // ── Helpers ────────────────────────────────────────────────────────────────

  private LoginResponse buildLoginResponse(UsersEntity user, WalletsEntity wallet) {
    String accessToken = jwtService.generateAccessToken(user.getId());
    String refreshToken = jwtService.generateRefreshToken(user.getId());
    return LoginResponse.builder()
        .accessToken(accessToken)
        .refreshToken(refreshToken)
        .user(toUserSummary(user, wallet))
        .build();
  }

  private UserSummary toUserSummary(UsersEntity user, WalletsEntity wallet) {
    return UserSummary.builder()
        .id(user.getId())
        .name(user.getName())
        .mobile(user.getMobile())
        .email(user.getEmail())
        .role(user.getRole() != null ? user.getRole().name() : null)
        .roles(user.getRoles())
        .status(user.getStatus() != null ? user.getStatus().name() : null)
        .mediatorCode(user.getMediatorCode())
        .parentCode(user.getParentCode())
        .isVerifiedByMediator(user.getIsVerifiedByMediator())
        .brandCode(user.getBrandCode())
        .kycStatus(user.getKycStatus() != null ? user.getKycStatus().name() : null)
        .upiId(user.getUpiId())
        .bankAccountNumber(user.getBankAccountNumber())
        .bankIfsc(user.getBankIfsc())
        .bankName(user.getBankName())
        .bankHolderName(user.getBankHolderName())
        .avatar(user.getAvatar())
        .walletAvailablePaise(wallet != null ? wallet.getAvailablePaise() : 0)
        .walletPendingPaise(wallet != null ? wallet.getPendingPaise() : 0)
        .createdAt(user.getCreatedAt())
        .build();
  }
}
