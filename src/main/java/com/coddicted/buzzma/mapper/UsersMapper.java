package com.coddicted.buzzma.mapper;

import com.coddicted.buzzma.dto.UsersRequestDto;
import com.coddicted.buzzma.dto.UsersResponseDto;
import com.coddicted.buzzma.entity.UsersEntity;
import org.mapstruct.Mapper;
import org.mapstruct.MappingTarget;
import org.mapstruct.NullValuePropertyMappingStrategy;

@Mapper(
    componentModel = "spring",
    nullValuePropertyMappingStrategy = NullValuePropertyMappingStrategy.IGNORE)
public interface UsersMapper {
  UsersEntity toEntity(UsersRequestDto request);

  UsersResponseDto toResponse(UsersEntity entity);

  void update(UsersRequestDto request, @MappingTarget UsersEntity entity);
}
