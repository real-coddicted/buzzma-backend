package com.coddicted.buzzma.mapper;

import com.coddicted.buzzma.dto.InvitesRequestDto;
import com.coddicted.buzzma.dto.InvitesResponseDto;
import com.coddicted.buzzma.entity.InvitesEntity;
import org.mapstruct.Mapper;
import org.mapstruct.MappingTarget;
import org.mapstruct.NullValuePropertyMappingStrategy;

@Mapper(
    componentModel = "spring",
    nullValuePropertyMappingStrategy = NullValuePropertyMappingStrategy.IGNORE)
public interface InvitesMapper {
  InvitesEntity toEntity(InvitesRequestDto request);

  InvitesResponseDto toResponse(InvitesEntity entity);

  void update(InvitesRequestDto request, @MappingTarget InvitesEntity entity);
}
