package com.coddicted.buzzma.mapper;

import com.coddicted.buzzma.dto.TicketsRequestDto;
import com.coddicted.buzzma.dto.TicketsResponseDto;
import com.coddicted.buzzma.entity.TicketsEntity;
import org.mapstruct.Mapper;
import org.mapstruct.MappingTarget;
import org.mapstruct.NullValuePropertyMappingStrategy;

@Mapper(
    componentModel = "spring",
    nullValuePropertyMappingStrategy = NullValuePropertyMappingStrategy.IGNORE)
public interface TicketsMapper {
  TicketsEntity toEntity(TicketsRequestDto request);

  TicketsResponseDto toResponse(TicketsEntity entity);

  void update(TicketsRequestDto request, @MappingTarget TicketsEntity entity);
}
