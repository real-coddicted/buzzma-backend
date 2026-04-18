package com.mobo.buyers.mapper;

import com.mobo.buyers.api.ShopperProfilesRequestDto;
import com.mobo.buyers.api.ShopperProfilesResponseDto;
import com.mobo.buyers.persistence.ShopperProfilesEntity;
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
