package com.coddicted.buzzma.admin.web;

import com.coddicted.buzzma.admin.service.AdminDomainService;
import com.coddicted.buzzma.identity.api.UsersResponseDto;
import com.coddicted.buzzma.shared.security.CurrentUserId;
import jakarta.validation.constraints.Max;
import jakarta.validation.constraints.Min;
import jakarta.validation.constraints.Size;
import java.util.List;
import java.util.UUID;
import org.springframework.http.HttpStatus;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.ResponseStatus;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/api/admin")
@Validated
@PreAuthorize("hasRole('admin')")
public class AdminController {

  private final AdminDomainService adminDomainService;

  public AdminController(AdminDomainService adminDomainService) {
    this.adminDomainService = adminDomainService;
  }

  @PostMapping("/suspend/{targetId}")
  @ResponseStatus(HttpStatus.NO_CONTENT)
  public void suspendUser(
      @PathVariable UUID targetId,
      @RequestParam(required = false) @Size(max = 500) String reason,
      @CurrentUserId UUID adminId) {
    adminDomainService.suspendUser(targetId, reason, adminId);
  }

  @PostMapping("/unsuspend/{targetId}")
  @ResponseStatus(HttpStatus.NO_CONTENT)
  public void unsuspendUser(
      @PathVariable UUID targetId,
      @RequestParam(required = false) @Size(max = 500) String reason,
      @CurrentUserId UUID adminId) {
    adminDomainService.unsuspendUser(targetId, reason, adminId);
  }

  @GetMapping("/users")
  public List<UsersResponseDto> getUsers(
      @RequestParam(defaultValue = "50") @Min(1) @Max(500) int limit,
      @RequestParam(defaultValue = "0") @Min(0) int offset) {
    return adminDomainService.getUsers(limit, offset);
  }

  @GetMapping("/users/{id}")
  public UsersResponseDto getUserById(@PathVariable UUID id) {
    return adminDomainService.getUserById(id);
  }

  @DeleteMapping("/users/{id}")
  @ResponseStatus(HttpStatus.NO_CONTENT)
  public void deleteUser(@PathVariable UUID id, @CurrentUserId UUID adminId) {
    adminDomainService.deleteUser(id, adminId);
  }
}
