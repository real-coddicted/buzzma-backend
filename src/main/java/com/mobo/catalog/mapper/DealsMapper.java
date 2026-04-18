package com.mobo.catalog.mapper;

import com.mobo.catalog.api.DealsRequestDto;
import com.mobo.catalog.api.DealsResponseDto;
import com.mobo.catalog.persistence.DealsEntity;
import org.mapstruct.BeanMapping;
import org.mapstruct.Mapper;
import org.mapstruct.MappingTarget;
import org.mapstruct.NullValuePropertyMappingStrategy;

@Mapper(
    componentModel = "spring",
    nullValuePropertyMappingStrategy = NullValuePropertyMappingStrategy.IGNORE)
public interface DealsMapper {

  DealsEntity toEntity(DealsRequestDto request);

  DealsResponseDto toResponse(DealsEntity entity);

  @BeanMapping(nullValuePropertyMappingStrategy = NullValuePropertyMappingStrategy.IGNORE)
  void update(DealsRequestDto request, @MappingTarget DealsEntity entity);
}
