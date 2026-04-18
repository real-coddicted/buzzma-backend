package com.coddicted.buzzma.brands.service;

import com.coddicted.buzzma.brands.api.BrandsRequestDto;
import com.coddicted.buzzma.brands.api.BrandsResponseDto;
import java.util.List;
import java.util.UUID;

public interface BrandService {

  List<BrandsResponseDto> list(int limit, int offset);

  BrandsResponseDto getById(UUID id);

  BrandsResponseDto create(BrandsRequestDto request);

  BrandsResponseDto update(UUID id, BrandsRequestDto request);

  void delete(UUID id);
}
