package com.mobo.notifications.service;

import com.mobo.notifications.api.PushSubscriptionsRequestDto;
import com.mobo.notifications.api.PushSubscriptionsResponseDto;
import java.util.List;
import java.util.UUID;

public interface PushSubscriptionService {

  List<PushSubscriptionsResponseDto> list(int limit, int offset);

  PushSubscriptionsResponseDto getById(UUID id);

  PushSubscriptionsResponseDto create(PushSubscriptionsRequestDto request);

  PushSubscriptionsResponseDto update(UUID id, PushSubscriptionsRequestDto request);

  void delete(UUID id);
}
