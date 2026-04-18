package com.coddicted.buzzma.brands.mapper;

import com.coddicted.buzzma.brands.api.BrandsRequestDto;
import com.coddicted.buzzma.brands.api.BrandsResponseDto;
import com.coddicted.buzzma.brands.persistence.BrandsEntity;
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
