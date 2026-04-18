package com.mobo.notifications.mapper;

import com.mobo.notifications.api.PushSubscriptionsRequestDto;
import com.mobo.notifications.api.PushSubscriptionsResponseDto;
import com.mobo.notifications.persistence.PushSubscriptionsEntity;
import org.mapstruct.BeanMapping;
import org.mapstruct.Mapper;
import org.mapstruct.MappingTarget;
import org.mapstruct.NullValuePropertyMappingStrategy;

@Mapper(
    componentModel = "spring",
    nullValuePropertyMappingStrategy = NullValuePropertyMappingStrategy.IGNORE)
public interface PushSubscriptionsMapper {

  PushSubscriptionsEntity toEntity(PushSubscriptionsRequestDto request);

  PushSubscriptionsResponseDto toResponse(PushSubscriptionsEntity entity);

  @BeanMapping(nullValuePropertyMappingStrategy = NullValuePropertyMappingStrategy.IGNORE)
  void update(PushSubscriptionsRequestDto request, @MappingTarget PushSubscriptionsEntity entity);
}
