package com.mobo.controller;

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
import com.mobo.security.CurrentUserId;
import com.mobo.service.AuthService;
import jakarta.validation.Valid;
import java.util.UUID;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PatchMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestHeader;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.ResponseStatus;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/api/auth")
public class AuthController {

  private final AuthService authService;

  public AuthController(AuthService authService) {
    this.authService = authService;
  }

  @PostMapping("/register")
  @ResponseStatus(HttpStatus.CREATED)
  public LoginResponse register(@Valid @RequestBody RegisterRequest request) {
    return authService.register(request);
  }

  @PostMapping("/login")
  public LoginResponse login(@Valid @RequestBody LoginRequest request) {
    return authService.login(request);
  }

  @PostMapping("/refresh")
  public LoginResponse refresh(@RequestHeader("X-Refresh-Token") String refreshToken) {
    return authService.refresh(refreshToken);
  }

  @GetMapping("/me")
  @PreAuthorize("isAuthenticated()")
  public UserSummary me(@CurrentUserId UUID userId) {
    return authService.me(userId);
  }

  @PostMapping("/register-ops")
  @ResponseStatus(HttpStatus.CREATED)
  public LoginResponse registerOps(@Valid @RequestBody RegisterOpsRequest request) {
    return authService.registerOps(request);
  }

  @PostMapping("/register-brand")
  @ResponseStatus(HttpStatus.CREATED)
  public LoginResponse registerBrand(@Valid @RequestBody RegisterBrandRequest request) {
    return authService.registerBrand(request);
  }

  @PatchMapping("/profile")
  @PreAuthorize("isAuthenticated()")
  public UserSummary updateProfile(
      @CurrentUserId UUID userId, @Valid @RequestBody UpdateProfileRequest request) {
    return authService.updateProfile(userId, request);
  }

  @PostMapping("/security-questions")
  @ResponseStatus(HttpStatus.NO_CONTENT)
  @PreAuthorize("isAuthenticated()")
  public ResponseEntity<Void> saveSecurityQuestions(
      @CurrentUserId UUID userId, @Valid @RequestBody SecurityQuestionsRequest request) {
    authService.saveSecurityQuestions(userId, request);
    return ResponseEntity.noContent().build();
  }

  @PostMapping("/forgot-password/lookup")
  public UserSummary forgotPasswordLookup(@Valid @RequestBody ForgotPasswordLookupRequest request) {
    return authService.forgotPasswordLookup(request);
  }

  @PostMapping("/forgot-password/reset")
  @ResponseStatus(HttpStatus.NO_CONTENT)
  public ResponseEntity<Void> forgotPasswordReset(
      @Valid @RequestBody ForgotPasswordResetRequest request) {
    authService.forgotPasswordReset(request);
    return ResponseEntity.noContent().build();
  }
}
