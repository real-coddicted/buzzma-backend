package com.mobo.service;

import com.mobo.dto.PushSubscriptionsRequestDto;
import com.mobo.dto.PushSubscriptionsResponseDto;
import java.util.List;
import java.util.UUID;

public interface PushSubscriptionService {

  List<PushSubscriptionsResponseDto> list(int limit, int offset);

  PushSubscriptionsResponseDto getById(UUID id);

  PushSubscriptionsResponseDto create(PushSubscriptionsRequestDto request);

  PushSubscriptionsResponseDto update(UUID id, PushSubscriptionsRequestDto request);

  void delete(UUID id);
}
