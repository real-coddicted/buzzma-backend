package com.coddicted.buzzma.mapper;

import com.coddicted.buzzma.dto.PushSubscriptionsRequestDto;
import com.coddicted.buzzma.dto.PushSubscriptionsResponseDto;
import com.coddicted.buzzma.entity.PushSubscriptionsEntity;
import org.mapstruct.Mapper;
import org.mapstruct.MappingTarget;
import org.mapstruct.NullValuePropertyMappingStrategy;

@Mapper(
    componentModel = "spring",
    nullValuePropertyMappingStrategy = NullValuePropertyMappingStrategy.IGNORE)
public interface PushSubscriptionsMapper {
  PushSubscriptionsEntity toEntity(PushSubscriptionsRequestDto request);

  PushSubscriptionsResponseDto toResponse(PushSubscriptionsEntity entity);

  void update(PushSubscriptionsRequestDto request, @MappingTarget PushSubscriptionsEntity entity);
}
