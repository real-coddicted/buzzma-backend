package com.coddicted.buzzma.wallet.service;

import com.coddicted.buzzma.wallet.api.WalletsRequestDto;
import com.coddicted.buzzma.wallet.api.WalletsResponseDto;
import java.util.List;
import java.util.UUID;

public interface WalletService {

  List<WalletsResponseDto> list(int limit, int offset);

  WalletsResponseDto getById(UUID id);

  WalletsResponseDto create(WalletsRequestDto request);

  WalletsResponseDto update(UUID id, WalletsRequestDto request);

  void delete(UUID id);
}
