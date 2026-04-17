package com.mobo.service.impl;

import com.mobo.common.AuditLogWriter;
import com.mobo.common.OffsetBasedPageRequest;
import com.mobo.dto.UsersResponseDto;
import com.mobo.entity.SuspensionsEntity;
import com.mobo.entity.UsersEntity;
import com.mobo.entity.enums.SuspensionAction;
import com.mobo.entity.enums.UserStatus;
import com.mobo.exception.ApiException;
import com.mobo.mapper.UsersMapper;
import com.mobo.repository.SuspensionsRepository;
import com.mobo.repository.UsersRepository;
import com.mobo.service.AdminDomainService;
import com.mobo.service.OrderWorkflowService;
import java.util.List;
import java.util.UUID;
import java.util.stream.Collectors;
import org.springframework.data.domain.Sort;
import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
public class AdminDomainServiceImpl implements AdminDomainService {

  private final UsersRepository usersRepository;
  private final SuspensionsRepository suspensionsRepository;
  private final OrderWorkflowService orderWorkflowService;
  private final UsersMapper usersMapper;
  private final AuditLogWriter auditLogWriter;

  public AdminDomainServiceImpl(
      UsersRepository usersRepository,
      SuspensionsRepository suspensionsRepository,
      OrderWorkflowService orderWorkflowService,
      UsersMapper usersMapper,
      AuditLogWriter auditLogWriter) {
    this.usersRepository = usersRepository;
    this.suspensionsRepository = suspensionsRepository;
    this.orderWorkflowService = orderWorkflowService;
    this.usersMapper = usersMapper;
    this.auditLogWriter = auditLogWriter;
  }

  @Override
  @Transactional
  public void suspendUser(UUID targetId, String reason, UUID adminId) {
    UsersEntity user =
        usersRepository
            .findById(targetId)
            .filter(u -> !Boolean.TRUE.equals(u.getIsDeleted()))
            .orElseThrow(() -> new ApiException(HttpStatus.NOT_FOUND, "USER_NOT_FOUND"));

    if (user.getStatus() == UserStatus.suspended) {
      throw new ApiException(HttpStatus.CONFLICT, "USER_ALREADY_SUSPENDED");
    }

    user.setStatus(UserStatus.suspended);
    usersRepository.save(user);

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
    UsersEntity user =
        usersRepository
            .findById(targetId)
            .filter(u -> !Boolean.TRUE.equals(u.getIsDeleted()))
            .orElseThrow(() -> new ApiException(HttpStatus.NOT_FOUND, "USER_NOT_FOUND"));

    user.setStatus(UserStatus.active);
    usersRepository.save(user);

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
    var pageable =
        new OffsetBasedPageRequest(limit, offset, Sort.by(Sort.Direction.DESC, "createdAt"));
    return usersRepository.findAllByIsDeletedFalse(pageable).stream()
        .map(usersMapper::toResponse)
        .collect(Collectors.toList());
  }

  @Override
  @Transactional(readOnly = true)
  public UsersResponseDto getUserById(UUID id) {
    UsersEntity user =
        usersRepository
            .findById(id)
            .filter(u -> !Boolean.TRUE.equals(u.getIsDeleted()))
            .orElseThrow(() -> new ApiException(HttpStatus.NOT_FOUND, "USER_NOT_FOUND"));
    return usersMapper.toResponse(user);
  }

  @Override
  @Transactional
  public void deleteUser(UUID id, UUID adminId) {
    UsersEntity user =
        usersRepository
            .findById(id)
            .filter(u -> !Boolean.TRUE.equals(u.getIsDeleted()))
            .orElseThrow(() -> new ApiException(HttpStatus.NOT_FOUND, "USER_NOT_FOUND"));

    user.setIsDeleted(true);
    usersRepository.save(user);

    auditLogWriter.write(adminId, new String[] {"admin"}, "DELETE", "User", id.toString(), null);
  }
}
