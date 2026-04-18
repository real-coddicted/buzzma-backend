package com.coddicted.buzzma.admin.api;

import java.util.List;

public interface AuditLogQueryPort {

  List<AuditLogsResponseDto> listByEntity(
      String entityType, String entityId, int limit, int offset);
}
