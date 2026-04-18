package com.coddicted.buzzma.admin.mapper;

import com.coddicted.buzzma.admin.api.SystemConfigsRequestDto;
import com.coddicted.buzzma.admin.api.SystemConfigsResponseDto;
import com.coddicted.buzzma.admin.persistence.SystemConfigsEntity;
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
