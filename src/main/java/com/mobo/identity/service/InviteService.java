package com.mobo.identity.service;

import com.mobo.identity.api.InvitesRequestDto;
import com.mobo.identity.api.InvitesResponseDto;
import java.util.List;
import java.util.UUID;

public interface InviteService {

  List<InvitesResponseDto> list(int limit, int offset);

  InvitesResponseDto getById(UUID id);

  InvitesResponseDto create(InvitesRequestDto request);

  InvitesResponseDto update(UUID id, InvitesRequestDto request);

  void delete(UUID id);
}
