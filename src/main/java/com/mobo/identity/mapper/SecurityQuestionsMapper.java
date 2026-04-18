package com.mobo.identity.mapper;

import com.mobo.identity.api.SecurityQuestionsRequestDto;
import com.mobo.identity.api.SecurityQuestionsResponseDto;
import com.mobo.identity.persistence.SecurityQuestionsEntity;
import org.mapstruct.BeanMapping;
import org.mapstruct.Mapper;
import org.mapstruct.MappingTarget;
import org.mapstruct.NullValuePropertyMappingStrategy;

@Mapper(
    componentModel = "spring",
    nullValuePropertyMappingStrategy = NullValuePropertyMappingStrategy.IGNORE)
public interface SecurityQuestionsMapper {

  SecurityQuestionsEntity toEntity(SecurityQuestionsRequestDto request);

  SecurityQuestionsResponseDto toResponse(SecurityQuestionsEntity entity);

  @BeanMapping(nullValuePropertyMappingStrategy = NullValuePropertyMappingStrategy.IGNORE)
  void update(SecurityQuestionsRequestDto request, @MappingTarget SecurityQuestionsEntity entity);
}
