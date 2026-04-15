package com.coddicted.buzzma.mapper;

import com.coddicted.buzzma.dto.ShopperProfilesRequestDto;
import com.coddicted.buzzma.dto.ShopperProfilesResponseDto;
import com.coddicted.buzzma.entity.ShopperProfilesEntity;
import org.mapstruct.Mapper;
import org.mapstruct.MappingTarget;
import org.mapstruct.NullValuePropertyMappingStrategy;

@Mapper(
    componentModel = "spring",
    nullValuePropertyMappingStrategy = NullValuePropertyMappingStrategy.IGNORE)
public interface ShopperProfilesMapper {
  ShopperProfilesEntity toEntity(ShopperProfilesRequestDto request);

  ShopperProfilesResponseDto toResponse(ShopperProfilesEntity entity);

  void update(ShopperProfilesRequestDto request, @MappingTarget ShopperProfilesEntity entity);
}
