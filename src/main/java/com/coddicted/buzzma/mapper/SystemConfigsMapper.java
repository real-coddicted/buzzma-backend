package com.coddicted.buzzma.mapper;

import com.coddicted.buzzma.dto.SystemConfigsRequestDto;
import com.coddicted.buzzma.dto.SystemConfigsResponseDto;
import com.coddicted.buzzma.entity.SystemConfigsEntity;
import org.mapstruct.Mapper;
import org.mapstruct.MappingTarget;
import org.mapstruct.NullValuePropertyMappingStrategy;

@Mapper(
    componentModel = "spring",
    nullValuePropertyMappingStrategy = NullValuePropertyMappingStrategy.IGNORE)
public interface SystemConfigsMapper {
  SystemConfigsEntity toEntity(SystemConfigsRequestDto request);

  SystemConfigsResponseDto toResponse(SystemConfigsEntity entity);

  void update(SystemConfigsRequestDto request, @MappingTarget SystemConfigsEntity entity);
}
