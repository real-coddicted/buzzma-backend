package com.mobo.mapper;

import com.mobo.dto.PendingConnectionsRequestDto;
import com.mobo.dto.PendingConnectionsResponseDto;
import com.mobo.entity.PendingConnectionsEntity;
import org.mapstruct.BeanMapping;
import org.mapstruct.Mapper;
import org.mapstruct.MappingTarget;
import org.mapstruct.NullValuePropertyMappingStrategy;

@Mapper(
    componentModel = "spring",
    nullValuePropertyMappingStrategy = NullValuePropertyMappingStrategy.IGNORE)
public interface PendingConnectionsMapper {

  PendingConnectionsEntity toEntity(PendingConnectionsRequestDto request);

  PendingConnectionsResponseDto toResponse(PendingConnectionsEntity entity);

  @BeanMapping(nullValuePropertyMappingStrategy = NullValuePropertyMappingStrategy.IGNORE)
  void update(PendingConnectionsRequestDto request, @MappingTarget PendingConnectionsEntity entity);
}
