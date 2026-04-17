package com.mobo.mapper;

import com.mobo.dto.ShopperProfilesRequestDto;
import com.mobo.dto.ShopperProfilesResponseDto;
import com.mobo.entity.ShopperProfilesEntity;
import org.mapstruct.BeanMapping;
import org.mapstruct.Mapper;
import org.mapstruct.MappingTarget;
import org.mapstruct.NullValuePropertyMappingStrategy;

@Mapper(
    componentModel = "spring",
    nullValuePropertyMappingStrategy = NullValuePropertyMappingStrategy.IGNORE)
public interface ShopperProfilesMapper {

  ShopperProfilesEntity toEntity(ShopperProfilesRequestDto request);

  ShopperProfilesResponseDto toResponse(ShopperProfilesEntity entity);

  @BeanMapping(nullValuePropertyMappingStrategy = NullValuePropertyMappingStrategy.IGNORE)
  void update(ShopperProfilesRequestDto request, @MappingTarget ShopperProfilesEntity entity);
}
