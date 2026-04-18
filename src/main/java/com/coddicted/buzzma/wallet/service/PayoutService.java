package com.coddicted.buzzma.wallet.service;

import com.coddicted.buzzma.wallet.api.PayoutsRequestDto;
import com.coddicted.buzzma.wallet.api.PayoutsResponseDto;
import java.util.List;
import java.util.UUID;

public interface PayoutService {

  List<PayoutsResponseDto> list(int limit, int offset);

  PayoutsResponseDto getById(UUID id);

  PayoutsResponseDto create(PayoutsRequestDto request);

  PayoutsResponseDto update(UUID id, PayoutsRequestDto request);

  void delete(UUID id);
}
