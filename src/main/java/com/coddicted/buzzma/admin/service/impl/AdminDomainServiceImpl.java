package com.coddicted.buzzma.admin.service.impl;

import com.coddicted.buzzma.admin.persistence.SuspensionsEntity;
import com.coddicted.buzzma.admin.persistence.SuspensionsRepository;
import com.coddicted.buzzma.admin.service.AdminDomainService;
import com.coddicted.buzzma.identity.api.UserAdminPort;
import com.coddicted.buzzma.identity.api.UserQueryPort;
import com.coddicted.buzzma.identity.api.UsersResponseDto;
import com.coddicted.buzzma.orders.api.OrderWorkflowService;
import com.coddicted.buzzma.shared.common.AuditLogWriter;
import com.coddicted.buzzma.shared.enums.SuspensionAction;
import com.coddicted.buzzma.shared.enums.UserStatus;
import com.coddicted.buzzma.shared.exception.ApiException;
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
