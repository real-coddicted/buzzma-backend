package com.coddicted.buzzma.mapper;

import com.coddicted.buzzma.dto.PendingConnectionsRequestDto;
import com.coddicted.buzzma.dto.PendingConnectionsResponseDto;
import com.coddicted.buzzma.entity.PendingConnectionsEntity;
import org.mapstruct.Mapper;
import org.mapstruct.MappingTarget;
import org.mapstruct.NullValuePropertyMappingStrategy;

@Mapper(
    componentModel = "spring",
    nullValuePropertyMappingStrategy = NullValuePropertyMappingStrategy.IGNORE)
public interface PendingConnectionsMapper {
  PendingConnectionsEntity toEntity(PendingConnectionsRequestDto request);

  PendingConnectionsResponseDto toResponse(PendingConnectionsEntity entity);

  void update(PendingConnectionsRequestDto request, @MappingTarget PendingConnectionsEntity entity);
}
