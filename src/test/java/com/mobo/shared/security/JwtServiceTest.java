package com.mobo.shared.security;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

import io.jsonwebtoken.JwtException;
import java.util.UUID;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

class JwtServiceTest {

  private JwtService jwtService;

  @BeforeEach
  void setUp() {
    JwtProperties props = new JwtProperties();
    props.setAccessSecret("test-access-secret-must-be-32-chars!!!");
    props.setRefreshSecret("test-refresh-secret-must-be-32-chars!");
    props.setAccessExpiryMs(900_000L);
    props.setRefreshExpiryMs(604_800_000L);
    jwtService = new JwtService(props);
  }

  @Test
  void signAccessToken_thenValidate_returnsUserId() {
    UUID userId = UUID.randomUUID();
    String token = jwtService.generateAccessToken(userId);
    UUID result = jwtService.validateAccessToken(token);
    assertThat(result).isEqualTo(userId);
  }

  @Test
  void signRefreshToken_thenValidate_returnsUserId() {
    UUID userId = UUID.randomUUID();
    String token = jwtService.generateRefreshToken(userId);
    UUID result = jwtService.validateRefreshToken(token);
    assertThat(result).isEqualTo(userId);
  }

  @Test
  void validateAccessToken_withExpiredToken_throwsJwtException() {
    JwtProperties shortProps = new JwtProperties();
    shortProps.setAccessSecret("test-access-secret-must-be-32-chars!!!");
    shortProps.setRefreshSecret("test-refresh-secret-must-be-32-chars!");
    shortProps.setAccessExpiryMs(-1000L); // Already expired
    shortProps.setRefreshExpiryMs(604_800_000L);
    JwtService shortService = new JwtService(shortProps);

    UUID userId = UUID.randomUUID();
    String token = shortService.generateAccessToken(userId);

    assertThatThrownBy(() -> jwtService.validateAccessToken(token))
        .isInstanceOf(JwtException.class);
  }

  @Test
  void validateAccessToken_withTamperedToken_throwsJwtException() {
    UUID userId = UUID.randomUUID();
    String token = jwtService.generateAccessToken(userId);
    String tampered = token.substring(0, token.length() - 5) + "XXXXX";

    assertThatThrownBy(() -> jwtService.validateAccessToken(tampered))
        .isInstanceOf(JwtException.class);
  }
}
