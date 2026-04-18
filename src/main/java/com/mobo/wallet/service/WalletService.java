package com.mobo.wallet.service;

import com.mobo.wallet.api.WalletsRequestDto;
import com.mobo.wallet.api.WalletsResponseDto;
import java.util.List;
import java.util.UUID;

public interface WalletService {

  List<WalletsResponseDto> list(int limit, int offset);

  WalletsResponseDto getById(UUID id);

  WalletsResponseDto create(WalletsRequestDto request);

  WalletsResponseDto update(UUID id, WalletsRequestDto request);

  void delete(UUID id);
}
