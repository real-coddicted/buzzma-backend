package com.coddicted.buzzma.admin.mapper;

import com.coddicted.buzzma.admin.api.SuspensionsRequestDto;
import com.coddicted.buzzma.admin.api.SuspensionsResponseDto;
import com.coddicted.buzzma.admin.persistence.SuspensionsEntity;
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
