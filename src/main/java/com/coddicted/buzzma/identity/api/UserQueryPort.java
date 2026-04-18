package com.coddicted.buzzma.identity.api;

import com.coddicted.buzzma.shared.enums.UserStatus;
import java.util.List;
import java.util.Optional;
import java.util.UUID;

public interface UserQueryPort {

  Optional<UsersResponseDto> findById(UUID id);

  Optional<UsersResponseDto> findByMediatorCode(String mediatorCode);

  Optional<String> findBrandCodeById(UUID id);

  Optional<UserStatus> findStatusById(UUID id);

  String[] findConnectedAgenciesById(UUID id);

  List<UsersResponseDto> listAll(int limit, int offset);

  List<UsersResponseDto> listByParentCode(String parentCode, int limit, int offset);

  List<UsersResponseDto> listByParentCodeAndVerified(
      String parentCode, boolean verified, int limit, int offset);

  long count();
}
