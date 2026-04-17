package com.mobo.mapper;

import com.mobo.dto.TransactionsRequestDto;
import com.mobo.dto.TransactionsResponseDto;
import com.mobo.entity.TransactionsEntity;
import org.mapstruct.BeanMapping;
import org.mapstruct.Mapper;
import org.mapstruct.MappingTarget;
import org.mapstruct.NullValuePropertyMappingStrategy;

@Mapper(
    componentModel = "spring",
    nullValuePropertyMappingStrategy = NullValuePropertyMappingStrategy.IGNORE)
public interface TransactionsMapper {

  TransactionsEntity toEntity(TransactionsRequestDto request);

  TransactionsResponseDto toResponse(TransactionsEntity entity);

  @BeanMapping(nullValuePropertyMappingStrategy = NullValuePropertyMappingStrategy.IGNORE)
  void update(TransactionsRequestDto request, @MappingTarget TransactionsEntity entity);
}
