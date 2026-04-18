package com.mobo.admin.mapper;

import com.mobo.admin.api.SystemConfigsRequestDto;
import com.mobo.admin.api.SystemConfigsResponseDto;
import com.mobo.admin.persistence.SystemConfigsEntity;
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
