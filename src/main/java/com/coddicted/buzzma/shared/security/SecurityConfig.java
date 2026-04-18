package com.coddicted.buzzma.shared.security;

import com.coddicted.buzzma.identity.security.JwtAuthenticationFilter;
import com.coddicted.buzzma.mediator.security.UpstreamSuspensionFilter;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.security.config.annotation.method.configuration.EnableMethodSecurity;
import org.springframework.security.config.annotation.web.builders.HttpSecurity;
import org.springframework.security.config.annotation.web.configuration.EnableWebSecurity;
import org.springframework.security.config.http.SessionCreationPolicy;
import org.springframework.security.web.SecurityFilterChain;
import org.springframework.security.web.authentication.UsernamePasswordAuthenticationFilter;

@Configuration
@EnableWebSecurity
@EnableMethodSecurity
public class SecurityConfig {

  private final JwtAuthenticationFilter jwtAuthenticationFilter;
  private final UpstreamSuspensionFilter upstreamSuspensionFilter;

  public SecurityConfig(
      JwtAuthenticationFilter jwtAuthenticationFilter,
      UpstreamSuspensionFilter upstreamSuspensionFilter) {
    this.jwtAuthenticationFilter = jwtAuthenticationFilter;
    this.upstreamSuspensionFilter = upstreamSuspensionFilter;
  }

  @Bean
  public SecurityFilterChain filterChain(HttpSecurity http) throws Exception {
    http.csrf(csrf -> csrf.disable())
        .sessionManagement(
            session -> session.sessionCreationPolicy(SessionCreationPolicy.STATELESS))
        .authorizeHttpRequests(
            auth ->
                auth.requestMatchers("/api/auth/**", "/api/health/**")
                    .permitAll()
                    .anyRequest()
                    .authenticated())
        .addFilterBefore(jwtAuthenticationFilter, UsernamePasswordAuthenticationFilter.class)
        .addFilterAfter(upstreamSuspensionFilter, JwtAuthenticationFilter.class);
    return http.build();
  }
}
