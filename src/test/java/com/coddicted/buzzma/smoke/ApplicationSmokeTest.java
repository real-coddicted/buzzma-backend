package com.coddicted.buzzma.smoke;

import static org.assertj.core.api.Assertions.assertThat;

import com.coddicted.buzzma.shared.security.JwtService;
import java.time.Instant;
import java.util.Map;
import java.util.UUID;
import java.util.concurrent.atomic.AtomicReference;
import java.util.stream.Stream;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.Arguments;
import org.junit.jupiter.params.provider.MethodSource;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.test.web.client.TestRestTemplate;
import org.springframework.http.HttpEntity;
import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpMethod;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.test.context.DynamicPropertyRegistry;
import org.springframework.test.context.DynamicPropertySource;
import org.testcontainers.containers.PostgreSQLContainer;

@SpringBootTest(webEnvironment = SpringBootTest.WebEnvironment.RANDOM_PORT)
class ApplicationSmokeTest {

  static final PostgreSQLContainer<?> POSTGRES =
      new PostgreSQLContainer<>("postgres:16-alpine")
          .withDatabaseName("mobo_smoke")
          .withUsername("mobo")
          .withPassword("mobo");

  static {
    POSTGRES.start();
  }

  @DynamicPropertySource
  static void props(DynamicPropertyRegistry registry) {
    registry.add("spring.datasource.url", POSTGRES::getJdbcUrl);
    registry.add("spring.datasource.username", POSTGRES::getUsername);
    registry.add("spring.datasource.password", POSTGRES::getPassword);
    registry.add("app.jwt.access-secret", () -> "smoke-test-access-secret-min-32-chars!!");
    registry.add("app.jwt.refresh-secret", () -> "smoke-test-refresh-secret-min-32-chars!!");
  }

  private static final AtomicReference<String> CACHED_TOKEN = new AtomicReference<>();

  @Autowired private TestRestTemplate rest;
  @Autowired private JdbcTemplate jdbc;
  @Autowired private JwtService jwtService;

  private String token() {
    String cached = CACHED_TOKEN.get();
    if (cached != null) {
      return cached;
    }
    UUID id = UUID.randomUUID();
    Instant now = Instant.now();
    jdbc.update(
        "INSERT INTO users "
            + "(id, name, mobile, password_hash, role, roles, status, kyc_status, "
            + " is_deleted, created_at, updated_at) "
            + "VALUES (?, ?, ?, ?, ?::user_role, ?::user_role[], ?::user_status, ?::kyc_status, "
            + " ?, ?, ?)",
        id,
        "Smoke Admin",
        "9999999999",
        "$2a$10$placeholder-not-used-in-this-test-path",
        "admin",
        "{admin}",
        "active",
        "verified",
        false,
        java.sql.Timestamp.from(now),
        java.sql.Timestamp.from(now));
    String fresh = jwtService.generateAccessToken(id);
    CACHED_TOKEN.set(fresh);
    return fresh;
  }

  @Test
  void healthEndpointReturnsOkAndExpectedShape() {
    @SuppressWarnings("rawtypes")
    ResponseEntity<Map> response = rest.getForEntity("/api/health", Map.class);

    assertThat(response.getStatusCode()).isEqualTo(HttpStatus.OK);
    assertThat(response.getBody())
        .isNotNull()
        .containsKeys("status", "uptime", "db")
        .containsEntry("status", "ok")
        .containsEntry("db", "up");
  }

  @ParameterizedTest(name = "{0} -> {1}")
  @MethodSource("moduleListEndpoints")
  void moduleListEndpointIsReachableAndReturns2xx(String module, String path) {
    HttpHeaders headers = new HttpHeaders();
    headers.setBearerAuth(token());

    ResponseEntity<String> response =
        rest.exchange(path, HttpMethod.GET, new HttpEntity<>(headers), String.class);

    assertThat(response.getStatusCode().is2xxSuccessful())
        .as("%s (%s) returned %s", module, path, response.getStatusCode())
        .isTrue();
  }

  static Stream<Arguments> moduleListEndpoints() {
    return Stream.of(
        Arguments.of("identity", "/api/users"),
        Arguments.of("brands", "/api/brands"),
        Arguments.of("agency", "/api/agencies"),
        Arguments.of("mediator", "/api/mediator-profiles"),
        Arguments.of("buyers", "/api/shopper-profiles"),
        Arguments.of("catalog", "/api/campaigns"),
        Arguments.of("orders", "/api/orders"),
        Arguments.of("wallet", "/api/wallets"),
        Arguments.of("support", "/api/tickets"),
        Arguments.of("admin", "/api/audit-logs"),
        Arguments.of("notifications", "/api/push-subscriptions"));
  }
}
