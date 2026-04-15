package com.coddicted.buzzma.mapper;

import com.coddicted.buzzma.dto.DealsRequestDto;
import com.coddicted.buzzma.dto.DealsResponseDto;
import com.coddicted.buzzma.entity.DealsEntity;
import org.mapstruct.Mapper;
import org.mapstruct.MappingTarget;
import org.mapstruct.NullValuePropertyMappingStrategy;

@Mapper(
    componentModel = "spring",
    nullValuePropertyMappingStrategy = NullValuePropertyMappingStrategy.IGNORE)
public interface DealsMapper {
  DealsEntity toEntity(DealsRequestDto request);

  DealsResponseDto toResponse(DealsEntity entity);

  void update(DealsRequestDto request, @MappingTarget DealsEntity entity);
}
