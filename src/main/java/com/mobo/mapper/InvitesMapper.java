package com.mobo.mapper;

import com.mobo.dto.InvitesRequestDto;
import com.mobo.dto.InvitesResponseDto;
import com.mobo.entity.InvitesEntity;
import org.mapstruct.BeanMapping;
import org.mapstruct.Mapper;
import org.mapstruct.MappingTarget;
import org.mapstruct.NullValuePropertyMappingStrategy;

@Mapper(
    componentModel = "spring",
    nullValuePropertyMappingStrategy = NullValuePropertyMappingStrategy.IGNORE)
public interface InvitesMapper {

  InvitesEntity toEntity(InvitesRequestDto request);

  InvitesResponseDto toResponse(InvitesEntity entity);

  @BeanMapping(nullValuePropertyMappingStrategy = NullValuePropertyMappingStrategy.IGNORE)
  void update(InvitesRequestDto request, @MappingTarget InvitesEntity entity);
}
