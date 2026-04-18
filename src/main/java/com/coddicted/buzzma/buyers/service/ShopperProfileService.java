package com.coddicted.buzzma.buyers.service;

import com.coddicted.buzzma.buyers.api.ShopperProfilesRequestDto;
import com.coddicted.buzzma.buyers.api.ShopperProfilesResponseDto;
import java.util.List;
import java.util.UUID;

public interface ShopperProfileService {

  List<ShopperProfilesResponseDto> list(int limit, int offset);

  ShopperProfilesResponseDto getById(UUID id);

  ShopperProfilesResponseDto create(ShopperProfilesRequestDto request);

  ShopperProfilesResponseDto update(UUID id, ShopperProfilesRequestDto request);

  void delete(UUID id);
}
