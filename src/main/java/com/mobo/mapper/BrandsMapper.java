package com.mobo.mapper;

import com.mobo.dto.BrandsRequestDto;
import com.mobo.dto.BrandsResponseDto;
import com.mobo.entity.BrandsEntity;
import org.mapstruct.BeanMapping;
import org.mapstruct.Mapper;
import org.mapstruct.MappingTarget;
import org.mapstruct.NullValuePropertyMappingStrategy;

@Mapper(
    componentModel = "spring",
    nullValuePropertyMappingStrategy = NullValuePropertyMappingStrategy.IGNORE)
public interface BrandsMapper {

  BrandsEntity toEntity(BrandsRequestDto request);

  BrandsResponseDto toResponse(BrandsEntity entity);

  @BeanMapping(nullValuePropertyMappingStrategy = NullValuePropertyMappingStrategy.IGNORE)
  void update(BrandsRequestDto request, @MappingTarget BrandsEntity entity);
}
