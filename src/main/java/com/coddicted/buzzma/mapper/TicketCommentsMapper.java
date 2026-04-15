package com.coddicted.buzzma.mapper;

import com.coddicted.buzzma.dto.TicketCommentsRequestDto;
import com.coddicted.buzzma.dto.TicketCommentsResponseDto;
import com.coddicted.buzzma.entity.TicketCommentsEntity;
import org.mapstruct.Mapper;
import org.mapstruct.MappingTarget;
import org.mapstruct.NullValuePropertyMappingStrategy;

@Mapper(
    componentModel = "spring",
    nullValuePropertyMappingStrategy = NullValuePropertyMappingStrategy.IGNORE)
public interface TicketCommentsMapper {
  TicketCommentsEntity toEntity(TicketCommentsRequestDto request);

  TicketCommentsResponseDto toResponse(TicketCommentsEntity entity);

  void update(TicketCommentsRequestDto request, @MappingTarget TicketCommentsEntity entity);
}
