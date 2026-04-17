package com.mobo.mapper;

import com.mobo.dto.SuspensionsRequestDto;
import com.mobo.dto.SuspensionsResponseDto;
import com.mobo.entity.SuspensionsEntity;
import org.mapstruct.BeanMapping;
import org.mapstruct.Mapper;
import org.mapstruct.MappingTarget;
import org.mapstruct.NullValuePropertyMappingStrategy;

@Mapper(
    componentModel = "spring",
    nullValuePropertyMappingStrategy = NullValuePropertyMappingStrategy.IGNORE)
public interface SuspensionsMapper {

  SuspensionsEntity toEntity(SuspensionsRequestDto request);

  SuspensionsResponseDto toResponse(SuspensionsEntity entity);

  @BeanMapping(nullValuePropertyMappingStrategy = NullValuePropertyMappingStrategy.IGNORE)
  void update(SuspensionsRequestDto request, @MappingTarget SuspensionsEntity entity);
}
