package com.mobo.mediator.api;

import java.util.UUID;

public interface MediatorAdminPort {

  void requestBrandConnection(
      UUID brandUserId, String brandCode, String agencyCode, String agencyName, UUID actorUserId);
}
