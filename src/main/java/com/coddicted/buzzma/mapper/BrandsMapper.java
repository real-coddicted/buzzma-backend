package com.coddicted.buzzma.mapper;

import com.coddicted.buzzma.dto.BrandsRequestDto;
import com.coddicted.buzzma.dto.BrandsResponseDto;
import com.coddicted.buzzma.entity.BrandsEntity;
import org.mapstruct.Mapper;
import org.mapstruct.MappingTarget;
import org.mapstruct.NullValuePropertyMappingStrategy;

@Mapper(
    componentModel = "spring",
    nullValuePropertyMappingStrategy = NullValuePropertyMappingStrategy.IGNORE)
public interface BrandsMapper {
  BrandsEntity toEntity(BrandsRequestDto request);

  BrandsResponseDto toResponse(BrandsEntity entity);

  void update(BrandsRequestDto request, @MappingTarget BrandsEntity entity);
}
