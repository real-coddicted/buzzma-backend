package com.mobo.mapper;

import com.mobo.dto.MediatorProfilesRequestDto;
import com.mobo.dto.MediatorProfilesResponseDto;
import com.mobo.entity.MediatorProfilesEntity;
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
