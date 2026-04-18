package com.mobo.support.mapper;

import com.mobo.support.api.TicketCommentsRequestDto;
import com.mobo.support.api.TicketCommentsResponseDto;
import com.mobo.support.persistence.TicketCommentsEntity;
import org.mapstruct.BeanMapping;
import org.mapstruct.Mapper;
import org.mapstruct.MappingTarget;
import org.mapstruct.NullValuePropertyMappingStrategy;

@Mapper(
    componentModel = "spring",
    nullValuePropertyMappingStrategy = NullValuePropertyMappingStrategy.IGNORE)
public interface TicketCommentsMapper {

  TicketCommentsEntity toEntity(TicketCommentsRequestDto request);

  TicketCommentsResponseDto toResponse(TicketCommentsEntity entity);

  @BeanMapping(nullValuePropertyMappingStrategy = NullValuePropertyMappingStrategy.IGNORE)
  void update(TicketCommentsRequestDto request, @MappingTarget TicketCommentsEntity entity);
}
