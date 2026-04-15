package com.coddicted.buzzma.mapper;

import com.coddicted.buzzma.dto.PayoutsRequestDto;
import com.coddicted.buzzma.dto.PayoutsResponseDto;
import com.coddicted.buzzma.entity.PayoutsEntity;
import org.mapstruct.Mapper;
import org.mapstruct.MappingTarget;
import org.mapstruct.NullValuePropertyMappingStrategy;

@Mapper(
    componentModel = "spring",
    nullValuePropertyMappingStrategy = NullValuePropertyMappingStrategy.IGNORE)
public interface PayoutsMapper {
  PayoutsEntity toEntity(PayoutsRequestDto request);

  PayoutsResponseDto toResponse(PayoutsEntity entity);

  void update(PayoutsRequestDto request, @MappingTarget PayoutsEntity entity);
}
