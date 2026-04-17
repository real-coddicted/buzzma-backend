package com.mobo.service;

import com.mobo.dto.TicketCommentsResponseDto;
import com.mobo.dto.TicketsResponseDto;
import java.util.UUID;

public interface TicketDomainService {

  TicketsResponseDto createTicket(
      UUID userId,
      String userName,
      String role,
      String orderId,
      String issueType,
      String description,
      String targetRole,
      String priority);

  TicketsResponseDto resolveTicket(UUID ticketId, String note, UUID resolvedBy);

  TicketsResponseDto rejectTicket(UUID ticketId, String note, UUID rejectedBy);

  TicketCommentsResponseDto addComment(
      UUID ticketId, String message, UUID userId, String userName, String role);
}
