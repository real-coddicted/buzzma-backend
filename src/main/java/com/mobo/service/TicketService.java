package com.mobo.service;

import com.mobo.dto.TicketsRequestDto;
import com.mobo.dto.TicketsResponseDto;
import java.util.List;
import java.util.UUID;

public interface TicketService {

  List<TicketsResponseDto> list(int limit, int offset);

  TicketsResponseDto getById(UUID id);

  TicketsResponseDto create(TicketsRequestDto request);

  TicketsResponseDto update(UUID id, TicketsRequestDto request);

  void delete(UUID id);
}
