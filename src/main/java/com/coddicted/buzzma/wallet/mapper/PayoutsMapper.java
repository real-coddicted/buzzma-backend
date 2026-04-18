package com.coddicted.buzzma.wallet.mapper;

import com.coddicted.buzzma.wallet.api.PayoutsRequestDto;
import com.coddicted.buzzma.wallet.api.PayoutsResponseDto;
import com.coddicted.buzzma.wallet.persistence.PayoutsEntity;
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
