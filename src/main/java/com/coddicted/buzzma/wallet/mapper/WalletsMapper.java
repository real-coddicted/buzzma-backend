package com.coddicted.buzzma.wallet.mapper;

import com.coddicted.buzzma.wallet.api.WalletsRequestDto;
import com.coddicted.buzzma.wallet.api.WalletsResponseDto;
import com.coddicted.buzzma.wallet.persistence.WalletsEntity;
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
