package com.coddicted.buzzma.identity.api.auth;

import lombok.Builder;
import lombok.Value;

@Value
@Builder
public class LoginResponse {

  String accessToken;

  String refreshToken;

  UserSummary user;
}
