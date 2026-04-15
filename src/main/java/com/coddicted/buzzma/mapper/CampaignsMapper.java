package com.coddicted.buzzma.mapper;

import com.coddicted.buzzma.dto.CampaignsRequestDto;
import com.coddicted.buzzma.dto.CampaignsResponseDto;
import com.coddicted.buzzma.entity.CampaignsEntity;
import org.mapstruct.Mapper;
import org.mapstruct.MappingTarget;
import org.mapstruct.NullValuePropertyMappingStrategy;

@Mapper(
    componentModel = "spring",
    nullValuePropertyMappingStrategy = NullValuePropertyMappingStrategy.IGNORE)
public interface CampaignsMapper {
  CampaignsEntity toEntity(CampaignsRequestDto request);

  CampaignsResponseDto toResponse(CampaignsEntity entity);

  void update(CampaignsRequestDto request, @MappingTarget CampaignsEntity entity);
}
