package com.mobo.wallet.mapper;

import com.mobo.wallet.api.PayoutsRequestDto;
import com.mobo.wallet.api.PayoutsResponseDto;
import com.mobo.wallet.persistence.PayoutsEntity;
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
