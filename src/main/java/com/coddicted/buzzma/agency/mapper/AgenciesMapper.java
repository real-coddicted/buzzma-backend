package com.coddicted.buzzma.agency.mapper;

import com.coddicted.buzzma.agency.api.AgenciesRequestDto;
import com.coddicted.buzzma.agency.api.AgenciesResponseDto;
import com.coddicted.buzzma.agency.persistence.AgenciesEntity;
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
