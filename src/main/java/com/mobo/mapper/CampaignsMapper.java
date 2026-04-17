package com.mobo.mapper;

import com.mobo.dto.CampaignsRequestDto;
import com.mobo.dto.CampaignsResponseDto;
import com.mobo.entity.CampaignsEntity;
import org.mapstruct.BeanMapping;
import org.mapstruct.Mapper;
import org.mapstruct.MappingTarget;
import org.mapstruct.NullValuePropertyMappingStrategy;

@Mapper(
    componentModel = "spring",
    nullValuePropertyMappingStrategy = NullValuePropertyMappingStrategy.IGNORE)
public interface CampaignsMapper {

  CampaignsEntity toEntity(CampaignsRequestDto request);

  CampaignsResponseDto toResponse(CampaignsEntity entity);

  @BeanMapping(nullValuePropertyMappingStrategy = NullValuePropertyMappingStrategy.IGNORE)
  void update(CampaignsRequestDto request, @MappingTarget CampaignsEntity entity);
}
