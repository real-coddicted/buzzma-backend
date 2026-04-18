package com.mobo.admin.service.impl;

import com.mobo.admin.persistence.SuspensionsEntity;
import com.mobo.admin.persistence.SuspensionsRepository;
import com.mobo.admin.service.AdminDomainService;
import com.mobo.identity.api.UserAdminPort;
import com.mobo.identity.api.UserQueryPort;
import com.mobo.identity.api.UsersResponseDto;
import com.mobo.orders.api.OrderWorkflowService;
import com.mobo.shared.common.AuditLogWriter;
import com.mobo.shared.enums.SuspensionAction;
import com.mobo.shared.enums.UserStatus;
import com.mobo.shared.exception.ApiException;
import java.util.List;
import java.util.UUID;
import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
public class AdminDomainServiceImpl implements AdminDomainService {

  private final UserQueryPort userQueryPort;
  private final UserAdminPort userAdminPort;
  private final SuspensionsRepository suspensionsRepository;
  private final OrderWorkflowService orderWorkflowService;
  private final AuditLogWriter auditLogWriter;

  public AdminDomainServiceImpl(
      UserQueryPort userQueryPort,
      UserAdminPort userAdminPort,
      SuspensionsRepository suspensionsRepository,
      OrderWorkflowService orderWorkflowService,
      AuditLogWriter auditLogWriter) {
    this.userQueryPort = userQueryPort;
    this.userAdminPort = userAdminPort;
    this.suspensionsRepository = suspensionsRepository;
    this.orderWorkflowService = orderWorkflowService;
    this.auditLogWriter = auditLogWriter;
  }

  @Override
  @Transactional
  public void suspendUser(UUID targetId, String reason, UUID adminId) {
    UserStatus status =
        userQueryPort
            .findStatusById(targetId)
            .orElseThrow(() -> new ApiException(HttpStatus.NOT_FOUND, "USER_NOT_FOUND"));

    if (status == UserStatus.suspended) {
      throw new ApiException(HttpStatus.CONFLICT, "USER_ALREADY_SUSPENDED");
    }

    userAdminPort.setStatus(targetId, UserStatus.suspended, adminId);

    SuspensionsEntity suspension = new SuspensionsEntity();
    suspension.setTargetUserId(targetId);
    suspension.setAction(SuspensionAction.suspend);
    suspension.setReason(reason);
    suspension.setAdminUserId(adminId);
    suspensionsRepository.save(suspension);

    orderWorkflowService.freezeByUserId(targetId, reason, adminId);

    auditLogWriter.write(
        adminId,
        new String[] {"admin"},
        "SUSPEND",
        "User",
        targetId.toString(),
        "{\"reason\":\"" + (reason != null ? reason.replace("\"", "\\\"") : "") + "\"}");
  }

  @Override
  @Transactional
  public void unsuspendUser(UUID targetId, String reason, UUID adminId) {
    if (userQueryPort.findStatusById(targetId).isEmpty()) {
      throw new ApiException(HttpStatus.NOT_FOUND, "USER_NOT_FOUND");
    }

    userAdminPort.setStatus(targetId, UserStatus.active, adminId);

    SuspensionsEntity suspension = new SuspensionsEntity();
    suspension.setTargetUserId(targetId);
    suspension.setAction(SuspensionAction.unsuspend);
    suspension.setReason(reason);
    suspension.setAdminUserId(adminId);
    suspensionsRepository.save(suspension);

    auditLogWriter.write(
        adminId,
        new String[] {"admin"},
        "UNSUSPEND",
        "User",
        targetId.toString(),
        "{\"reason\":\"" + (reason != null ? reason.replace("\"", "\\\"") : "") + "\"}");
  }

  @Override
  @Transactional(readOnly = true)
  public List<UsersResponseDto> getUsers(int limit, int offset) {
    return userQueryPort.listAll(limit, offset);
  }

  @Override
  @Transactional(readOnly = true)
  public UsersResponseDto getUserById(UUID id) {
    return userQueryPort
        .findById(id)
        .orElseThrow(() -> new ApiException(HttpStatus.NOT_FOUND, "USER_NOT_FOUND"));
  }

  @Override
  @Transactional
  public void deleteUser(UUID id, UUID adminId) {
    userAdminPort.markDeleted(id, adminId);

    auditLogWriter.write(adminId, new String[] {"admin"}, "DELETE", "User", id.toString(), null);
  }
}
