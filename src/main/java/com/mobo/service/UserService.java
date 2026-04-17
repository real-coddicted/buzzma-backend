package com.mobo.service;

import com.mobo.dto.UsersRequestDto;
import com.mobo.dto.UsersResponseDto;
import java.util.List;
import java.util.UUID;

public interface UserService {

  List<UsersResponseDto> list(int limit, int offset);

  UsersResponseDto getById(UUID id);

  UsersResponseDto create(UsersRequestDto request);

  UsersResponseDto update(UUID id, UsersRequestDto request);

  void delete(UUID id);
}
