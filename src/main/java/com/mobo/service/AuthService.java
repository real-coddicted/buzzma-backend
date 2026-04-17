package com.mobo.service;

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
