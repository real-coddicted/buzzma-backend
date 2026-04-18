package com.coddicted.buzzma.admin.service;

import com.coddicted.buzzma.identity.api.UsersResponseDto;
import java.util.List;
import java.util.UUID;

public interface AdminDomainService {

  void suspendUser(UUID targetId, String reason, UUID adminId);

  void unsuspendUser(UUID targetId, String reason, UUID adminId);

  List<UsersResponseDto> getUsers(int limit, int offset);

  UsersResponseDto getUserById(UUID id);

  void deleteUser(UUID id, UUID adminId);
}
