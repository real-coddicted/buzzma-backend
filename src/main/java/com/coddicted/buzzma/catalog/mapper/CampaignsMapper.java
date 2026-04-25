package com.coddicted.buzzma.catalog.mapper;

import com.coddicted.buzzma.catalog.api.CampaignsRequestDto;
import com.coddicted.buzzma.catalog.api.CampaignsResponseDto;
import com.coddicted.buzzma.catalog.persistence.CampaignsEntity;
import org.mapstruct.BeanMapping;
import org.mapstruct.Mapper;
import org.mapstruct.Mapping;
import org.mapstruct.MappingTarget;
import org.mapstruct.NullValuePropertyMappingStrategy;

@Mapper(
    componentModel = "spring",
    nullValuePropertyMappingStrategy = NullValuePropertyMappingStrategy.IGNORE)
public interface CampaignsMapper {

  CampaignsEntity toEntity(CampaignsRequestDto request);

  @Mapping(source = "allowedAgencyCodes", target = "allowedAgencies")
  CampaignsResponseDto toResponse(CampaignsEntity entity);

  @BeanMapping(nullValuePropertyMappingStrategy = NullValuePropertyMappingStrategy.IGNORE)
  void update(CampaignsRequestDto request, @MappingTarget CampaignsEntity entity);
}
