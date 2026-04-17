package com.mobo.mapper;

import com.mobo.dto.AgenciesRequestDto;
import com.mobo.dto.AgenciesResponseDto;
import com.mobo.entity.AgenciesEntity;
import org.mapstruct.BeanMapping;
import org.mapstruct.Mapper;
import org.mapstruct.MappingTarget;
import org.mapstruct.NullValuePropertyMappingStrategy;

@Mapper(
    componentModel = "spring",
    nullValuePropertyMappingStrategy = NullValuePropertyMappingStrategy.IGNORE)
public interface AgenciesMapper {

  AgenciesEntity toEntity(AgenciesRequestDto request);

  AgenciesResponseDto toResponse(AgenciesEntity entity);

  @BeanMapping(nullValuePropertyMappingStrategy = NullValuePropertyMappingStrategy.IGNORE)
  void update(AgenciesRequestDto request, @MappingTarget AgenciesEntity entity);
}
