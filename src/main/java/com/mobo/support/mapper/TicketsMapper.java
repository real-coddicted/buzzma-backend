package com.mobo.support.mapper;

import com.mobo.support.api.TicketsRequestDto;
import com.mobo.support.api.TicketsResponseDto;
import com.mobo.support.persistence.TicketsEntity;
import org.mapstruct.BeanMapping;
import org.mapstruct.Mapper;
import org.mapstruct.MappingTarget;
import org.mapstruct.NullValuePropertyMappingStrategy;

@Mapper(
    componentModel = "spring",
    nullValuePropertyMappingStrategy = NullValuePropertyMappingStrategy.IGNORE)
public interface TicketsMapper {

  TicketsEntity toEntity(TicketsRequestDto request);

  TicketsResponseDto toResponse(TicketsEntity entity);

  @BeanMapping(nullValuePropertyMappingStrategy = NullValuePropertyMappingStrategy.IGNORE)
  void update(TicketsRequestDto request, @MappingTarget TicketsEntity entity);
}
