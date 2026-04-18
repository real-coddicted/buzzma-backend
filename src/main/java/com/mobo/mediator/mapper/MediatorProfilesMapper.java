package com.mobo.mediator.mapper;

import com.mobo.mediator.api.MediatorProfilesRequestDto;
import com.mobo.mediator.api.MediatorProfilesResponseDto;
import com.mobo.mediator.persistence.MediatorProfilesEntity;
import org.mapstruct.BeanMapping;
import org.mapstruct.Mapper;
import org.mapstruct.MappingTarget;
import org.mapstruct.NullValuePropertyMappingStrategy;

@Mapper(
    componentModel = "spring",
    nullValuePropertyMappingStrategy = NullValuePropertyMappingStrategy.IGNORE)
public interface MediatorProfilesMapper {

  MediatorProfilesEntity toEntity(MediatorProfilesRequestDto request);

  MediatorProfilesResponseDto toResponse(MediatorProfilesEntity entity);

  @BeanMapping(nullValuePropertyMappingStrategy = NullValuePropertyMappingStrategy.IGNORE)
  void update(MediatorProfilesRequestDto request, @MappingTarget MediatorProfilesEntity entity);
}
