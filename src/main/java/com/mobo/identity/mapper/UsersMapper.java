package com.mobo.identity.mapper;

import com.mobo.identity.api.UsersRequestDto;
import com.mobo.identity.api.UsersResponseDto;
import com.mobo.identity.persistence.UsersEntity;
import org.mapstruct.BeanMapping;
import org.mapstruct.Mapper;
import org.mapstruct.MappingTarget;
import org.mapstruct.NullValuePropertyMappingStrategy;

@Mapper(
    componentModel = "spring",
    nullValuePropertyMappingStrategy = NullValuePropertyMappingStrategy.IGNORE)
public interface UsersMapper {

  UsersEntity toEntity(UsersRequestDto request);

  UsersResponseDto toResponse(UsersEntity entity);

  @BeanMapping(nullValuePropertyMappingStrategy = NullValuePropertyMappingStrategy.IGNORE)
  void update(UsersRequestDto request, @MappingTarget UsersEntity entity);
}
