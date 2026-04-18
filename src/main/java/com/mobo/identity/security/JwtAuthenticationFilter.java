package com.mobo.identity.security;

import com.mobo.identity.persistence.UsersRepository;
import com.mobo.shared.security.JwtService;
import io.jsonwebtoken.JwtException;
import jakarta.servlet.FilterChain;
import jakarta.servlet.ServletException;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import java.io.IOException;
import java.util.UUID;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.stereotype.Component;
import org.springframework.web.filter.OncePerRequestFilter;

@Component
public class JwtAuthenticationFilter extends OncePerRequestFilter {

  private final JwtService jwtService;
  private final UsersRepository usersRepository;

  public JwtAuthenticationFilter(JwtService jwtService, UsersRepository usersRepository) {
    this.jwtService = jwtService;
    this.usersRepository = usersRepository;
  }

  @Override
  protected void doFilterInternal(
      HttpServletRequest request, HttpServletResponse response, FilterChain filterChain)
      throws ServletException, IOException {

    String authHeader = request.getHeader("Authorization");
    if (authHeader == null || !authHeader.startsWith("Bearer ")) {
      filterChain.doFilter(request, response);
      return;
    }

    String token = authHeader.substring(7);
    try {
      UUID userId = jwtService.validateAccessToken(token);
      usersRepository
          .findById(userId)
          .ifPresent(
              user -> {
                MoboUserDetails userDetails = new MoboUserDetails(user);
                UsernamePasswordAuthenticationToken auth =
                    new UsernamePasswordAuthenticationToken(
                        userDetails, null, userDetails.getAuthorities());
                SecurityContextHolder.getContext().setAuthentication(auth);
              });
    } catch (JwtException | IllegalArgumentException ignored) {
      // Invalid token — let SecurityConfig handle 401
    }

    filterChain.doFilter(request, response);
  }
}
