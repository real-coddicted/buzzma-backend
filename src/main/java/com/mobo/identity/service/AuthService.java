package com.mobo.identity.service;

import com.mobo.identity.api.auth.ForgotPasswordLookupRequest;
import com.mobo.identity.api.auth.ForgotPasswordResetRequest;
import com.mobo.identity.api.auth.LoginRequest;
import com.mobo.identity.api.auth.LoginResponse;
import com.mobo.identity.api.auth.RegisterBrandRequest;
import com.mobo.identity.api.auth.RegisterOpsRequest;
import com.mobo.identity.api.auth.RegisterRequest;
import com.mobo.identity.api.auth.SecurityQuestionsRequest;
import com.mobo.identity.api.auth.UpdateProfileRequest;
import com.mobo.identity.api.auth.UserSummary;
import java.util.UUID;

public interface AuthService {

  LoginResponse register(RegisterRequest request);

  LoginResponse login(LoginRequest request);

  LoginResponse refresh(String refreshToken);

  UserSummary me(UUID userId);

  LoginResponse registerOps(RegisterOpsRequest request);

  LoginResponse registerBrand(RegisterBrandRequest request);

  UserSummary updateProfile(UUID userId, UpdateProfileRequest request);

  void saveSecurityQuestions(UUID userId, SecurityQuestionsRequest request);

  UserSummary forgotPasswordLookup(ForgotPasswordLookupRequest request);

  void forgotPasswordReset(ForgotPasswordResetRequest request);
}
