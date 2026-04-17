package com.mobo.security;

import com.mobo.service.LineageService;
import jakarta.servlet.FilterChain;
import jakarta.servlet.ServletException;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import java.io.IOException;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.stereotype.Component;
import org.springframework.web.filter.OncePerRequestFilter;

@Component
public class UpstreamSuspensionFilter extends OncePerRequestFilter {

  private final LineageService lineageService;

  public UpstreamSuspensionFilter(LineageService lineageService) {
    this.lineageService = lineageService;
  }

  @Override
  protected void doFilterInternal(
      HttpServletRequest request, HttpServletResponse response, FilterChain filterChain)
      throws ServletException, IOException {

    Authentication authentication = SecurityContextHolder.getContext().getAuthentication();
    if (authentication == null
        || !authentication.isAuthenticated()
        || !(authentication.getPrincipal() instanceof MoboUserDetails userDetails)) {
      filterChain.doFilter(request, response);
      return;
    }

    var user = userDetails.getUser();
    String[] roles = user.getRoles();
    if (roles == null) {
      filterChain.doFilter(request, response);
      return;
    }

    for (String role : roles) {
      if ("mediator".equals(role)) {
        String mediatorCode = user.getMediatorCode();
        if (mediatorCode != null && !lineageService.isMediatorActive(mediatorCode)) {
          response.setStatus(HttpServletResponse.SC_FORBIDDEN);
          response.getWriter().write("{\"error\":\"UPSTREAM_SUSPENDED\"}");
          return;
        }
        String parentCode = user.getParentCode();
        if (parentCode != null && !lineageService.isAgencyActive(parentCode)) {
          response.setStatus(HttpServletResponse.SC_FORBIDDEN);
          response.getWriter().write("{\"error\":\"UPSTREAM_SUSPENDED\"}");
          return;
        }
      } else if ("shopper".equals(role)) {
        // Check shopper's default mediator and its parent agency
        // Shopper suspension check delegated to application layer for performance
      }
    }

    filterChain.doFilter(request, response);
  }
}
