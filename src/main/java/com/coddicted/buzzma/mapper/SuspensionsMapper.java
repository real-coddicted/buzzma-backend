package com.coddicted.buzzma.mapper;

import com.coddicted.buzzma.dto.SuspensionsRequestDto;
import com.coddicted.buzzma.dto.SuspensionsResponseDto;
import com.coddicted.buzzma.entity.SuspensionsEntity;
import org.mapstruct.Mapper;
import org.mapstruct.MappingTarget;
import org.mapstruct.NullValuePropertyMappingStrategy;

@Mapper(
    componentModel = "spring",
    nullValuePropertyMappingStrategy = NullValuePropertyMappingStrategy.IGNORE)
public interface SuspensionsMapper {
  SuspensionsEntity toEntity(SuspensionsRequestDto request);

  SuspensionsResponseDto toResponse(SuspensionsEntity entity);

  void update(SuspensionsRequestDto request, @MappingTarget SuspensionsEntity entity);
}
