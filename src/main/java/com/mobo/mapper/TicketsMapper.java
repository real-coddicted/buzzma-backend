package com.mobo.mapper;

import com.mobo.dto.TicketsRequestDto;
import com.mobo.dto.TicketsResponseDto;
import com.mobo.entity.TicketsEntity;
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
