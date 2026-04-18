package com.coddicted.buzzma.buyers.mapper;

import com.coddicted.buzzma.buyers.api.ShopperProfilesRequestDto;
import com.coddicted.buzzma.buyers.api.ShopperProfilesResponseDto;
import com.coddicted.buzzma.buyers.persistence.ShopperProfilesEntity;
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
