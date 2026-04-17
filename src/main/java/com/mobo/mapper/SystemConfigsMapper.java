package com.mobo.mapper;

import com.mobo.dto.SystemConfigsRequestDto;
import com.mobo.dto.SystemConfigsResponseDto;
import com.mobo.entity.SystemConfigsEntity;
import org.mapstruct.BeanMapping;
import org.mapstruct.Mapper;
import org.mapstruct.MappingTarget;
import org.mapstruct.NullValuePropertyMappingStrategy;

@Mapper(
    componentModel = "spring",
    nullValuePropertyMappingStrategy = NullValuePropertyMappingStrategy.IGNORE)
public interface SystemConfigsMapper {

  SystemConfigsEntity toEntity(SystemConfigsRequestDto request);

  SystemConfigsResponseDto toResponse(SystemConfigsEntity entity);

  @BeanMapping(nullValuePropertyMappingStrategy = NullValuePropertyMappingStrategy.IGNORE)
  void update(SystemConfigsRequestDto request, @MappingTarget SystemConfigsEntity entity);
}
