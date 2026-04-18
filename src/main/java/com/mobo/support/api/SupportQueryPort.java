package com.mobo.support.api;

import com.mobo.shared.enums.TicketStatus;

public interface SupportQueryPort {

  boolean existsOpenTicketForOrder(String orderId, TicketStatus status);
}
