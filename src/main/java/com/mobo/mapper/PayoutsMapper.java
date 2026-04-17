package com.mobo.mapper;

import com.mobo.dto.PayoutsRequestDto;
import com.mobo.dto.PayoutsResponseDto;
import com.mobo.entity.PayoutsEntity;
import org.mapstruct.BeanMapping;
import org.mapstruct.Mapper;
import org.mapstruct.MappingTarget;
import org.mapstruct.NullValuePropertyMappingStrategy;

@Mapper(
    componentModel = "spring",
    nullValuePropertyMappingStrategy = NullValuePropertyMappingStrategy.IGNORE)
public interface PayoutsMapper {

  PayoutsEntity toEntity(PayoutsRequestDto request);

  PayoutsResponseDto toResponse(PayoutsEntity entity);

  @BeanMapping(nullValuePropertyMappingStrategy = NullValuePropertyMappingStrategy.IGNORE)
  void update(PayoutsRequestDto request, @MappingTarget PayoutsEntity entity);
}
