package com.coddicted.buzzma.mapper;

import com.coddicted.buzzma.dto.AgenciesRequestDto;
import com.coddicted.buzzma.dto.AgenciesResponseDto;
import com.coddicted.buzzma.entity.AgenciesEntity;
import org.mapstruct.Mapper;
import org.mapstruct.MappingTarget;
import org.mapstruct.NullValuePropertyMappingStrategy;

@Mapper(
    componentModel = "spring",
    nullValuePropertyMappingStrategy = NullValuePropertyMappingStrategy.IGNORE)
public interface AgenciesMapper {
  AgenciesEntity toEntity(AgenciesRequestDto request);

  AgenciesResponseDto toResponse(AgenciesEntity entity);

  void update(AgenciesRequestDto request, @MappingTarget AgenciesEntity entity);
}
