package com.coddicted.buzzma.mapper;

import com.coddicted.buzzma.dto.WalletsRequestDto;
import com.coddicted.buzzma.dto.WalletsResponseDto;
import com.coddicted.buzzma.entity.WalletsEntity;
import org.mapstruct.Mapper;
import org.mapstruct.MappingTarget;
import org.mapstruct.NullValuePropertyMappingStrategy;

@Mapper(
    componentModel = "spring",
    nullValuePropertyMappingStrategy = NullValuePropertyMappingStrategy.IGNORE)
public interface WalletsMapper {
  WalletsEntity toEntity(WalletsRequestDto request);

  WalletsResponseDto toResponse(WalletsEntity entity);

  void update(WalletsRequestDto request, @MappingTarget WalletsEntity entity);
}
