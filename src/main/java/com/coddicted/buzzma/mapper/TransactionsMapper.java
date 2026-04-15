package com.coddicted.buzzma.mapper;

import com.coddicted.buzzma.dto.TransactionsRequestDto;
import com.coddicted.buzzma.dto.TransactionsResponseDto;
import com.coddicted.buzzma.entity.TransactionsEntity;
import org.mapstruct.Mapper;
import org.mapstruct.MappingTarget;
import org.mapstruct.NullValuePropertyMappingStrategy;

@Mapper(
    componentModel = "spring",
    nullValuePropertyMappingStrategy = NullValuePropertyMappingStrategy.IGNORE)
public interface TransactionsMapper {
  TransactionsEntity toEntity(TransactionsRequestDto request);

  TransactionsResponseDto toResponse(TransactionsEntity entity);

  void update(TransactionsRequestDto request, @MappingTarget TransactionsEntity entity);
}
