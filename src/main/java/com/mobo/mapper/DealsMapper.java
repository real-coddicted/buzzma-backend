package com.mobo.mapper;

import com.mobo.dto.DealsRequestDto;
import com.mobo.dto.DealsResponseDto;
import com.mobo.entity.DealsEntity;
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
