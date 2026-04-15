package com.coddicted.buzzma.mapper;

import com.coddicted.buzzma.dto.SecurityQuestionsRequestDto;
import com.coddicted.buzzma.dto.SecurityQuestionsResponseDto;
import com.coddicted.buzzma.entity.SecurityQuestionsEntity;
import org.mapstruct.Mapper;
import org.mapstruct.MappingTarget;
import org.mapstruct.NullValuePropertyMappingStrategy;

@Mapper(
    componentModel = "spring",
    nullValuePropertyMappingStrategy = NullValuePropertyMappingStrategy.IGNORE)
public interface SecurityQuestionsMapper {
  SecurityQuestionsEntity toEntity(SecurityQuestionsRequestDto request);

  SecurityQuestionsResponseDto toResponse(SecurityQuestionsEntity entity);

  void update(SecurityQuestionsRequestDto request, @MappingTarget SecurityQuestionsEntity entity);
}
