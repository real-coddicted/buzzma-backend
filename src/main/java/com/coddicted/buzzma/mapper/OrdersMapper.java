package com.coddicted.buzzma.mapper;

import com.coddicted.buzzma.dto.OrdersRequestDto;
import com.coddicted.buzzma.dto.OrdersResponseDto;
import com.coddicted.buzzma.entity.OrdersEntity;
import org.mapstruct.Mapper;
import org.mapstruct.MappingTarget;
import org.mapstruct.NullValuePropertyMappingStrategy;

@Mapper(
    componentModel = "spring",
    nullValuePropertyMappingStrategy = NullValuePropertyMappingStrategy.IGNORE)
public interface OrdersMapper {
  OrdersEntity toEntity(OrdersRequestDto request);

  OrdersResponseDto toResponse(OrdersEntity entity);

  void update(OrdersRequestDto request, @MappingTarget OrdersEntity entity);
}
