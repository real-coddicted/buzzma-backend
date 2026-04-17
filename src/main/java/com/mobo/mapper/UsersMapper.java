package com.mobo.mapper;

import com.mobo.dto.UsersRequestDto;
import com.mobo.dto.UsersResponseDto;
import com.mobo.entity.UsersEntity;
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
