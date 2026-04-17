package com.mobo.service;

import com.mobo.dto.PendingConnectionsRequestDto;
import com.mobo.dto.PendingConnectionsResponseDto;
import java.util.List;
import java.util.UUID;

public interface PendingConnectionService {

  List<PendingConnectionsResponseDto> list(int limit, int offset);

  PendingConnectionsResponseDto getById(UUID id);

  PendingConnectionsResponseDto create(PendingConnectionsRequestDto request);

  PendingConnectionsResponseDto update(UUID id, PendingConnectionsRequestDto request);

  void delete(UUID id);
}
