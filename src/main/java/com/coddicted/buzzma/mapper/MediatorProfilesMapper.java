package com.coddicted.buzzma.mapper;

import com.coddicted.buzzma.dto.MediatorProfilesRequestDto;
import com.coddicted.buzzma.dto.MediatorProfilesResponseDto;
import com.coddicted.buzzma.entity.MediatorProfilesEntity;
import org.mapstruct.Mapper;
import org.mapstruct.MappingTarget;
import org.mapstruct.NullValuePropertyMappingStrategy;

@Mapper(
    componentModel = "spring",
    nullValuePropertyMappingStrategy = NullValuePropertyMappingStrategy.IGNORE)
public interface MediatorProfilesMapper {
  MediatorProfilesEntity toEntity(MediatorProfilesRequestDto request);

  MediatorProfilesResponseDto toResponse(MediatorProfilesEntity entity);

  void update(MediatorProfilesRequestDto request, @MappingTarget MediatorProfilesEntity entity);
}
