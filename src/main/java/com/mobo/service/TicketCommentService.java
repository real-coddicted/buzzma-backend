package com.mobo.service;

import com.mobo.dto.TicketCommentsRequestDto;
import com.mobo.dto.TicketCommentsResponseDto;
import java.util.List;
import java.util.UUID;

public interface TicketCommentService {

  List<TicketCommentsResponseDto> list(int limit, int offset);

  TicketCommentsResponseDto getById(UUID id);

  TicketCommentsResponseDto create(TicketCommentsRequestDto request);

  TicketCommentsResponseDto update(UUID id, TicketCommentsRequestDto request);

  void delete(UUID id);
}
