package com.mobo.wallet.mapper;

import com.mobo.wallet.api.TransactionsRequestDto;
import com.mobo.wallet.api.TransactionsResponseDto;
import com.mobo.wallet.persistence.TransactionsEntity;
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
