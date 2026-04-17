package com.mobo.mapper;

import com.mobo.dto.WalletsRequestDto;
import com.mobo.dto.WalletsResponseDto;
import com.mobo.entity.WalletsEntity;
import org.mapstruct.BeanMapping;
import org.mapstruct.Mapper;
import org.mapstruct.MappingTarget;
import org.mapstruct.NullValuePropertyMappingStrategy;

@Mapper(
    componentModel = "spring",
    nullValuePropertyMappingStrategy = NullValuePropertyMappingStrategy.IGNORE)
public interface WalletsMapper {

  WalletsEntity toEntity(WalletsRequestDto request);

  WalletsResponseDto toResponse(WalletsEntity entity);

  @BeanMapping(nullValuePropertyMappingStrategy = NullValuePropertyMappingStrategy.IGNORE)
  void update(WalletsRequestDto request, @MappingTarget WalletsEntity entity);
}
