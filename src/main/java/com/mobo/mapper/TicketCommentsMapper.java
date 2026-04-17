package com.mobo.mapper;

import com.mobo.dto.TicketCommentsRequestDto;
import com.mobo.dto.TicketCommentsResponseDto;
import com.mobo.entity.TicketCommentsEntity;
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
