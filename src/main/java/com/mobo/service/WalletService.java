package com.mobo.service;

import com.mobo.dto.WalletsRequestDto;
import com.mobo.dto.WalletsResponseDto;
import java.util.List;
import java.util.UUID;

public interface WalletService {

  List<WalletsResponseDto> list(int limit, int offset);

  WalletsResponseDto getById(UUID id);

  WalletsResponseDto create(WalletsRequestDto request);

  WalletsResponseDto update(UUID id, WalletsRequestDto request);

  void delete(UUID id);
}
