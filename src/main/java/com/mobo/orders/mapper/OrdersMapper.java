package com.mobo.orders.mapper;

import com.mobo.orders.api.OrdersRequestDto;
import com.mobo.orders.api.OrdersResponseDto;
import com.mobo.orders.persistence.OrdersEntity;
import org.mapstruct.BeanMapping;
import org.mapstruct.Mapper;
import org.mapstruct.MappingTarget;
import org.mapstruct.NullValuePropertyMappingStrategy;

@Mapper(
    componentModel = "spring",
    nullValuePropertyMappingStrategy = NullValuePropertyMappingStrategy.IGNORE)
public interface OrdersMapper {

  OrdersEntity toEntity(OrdersRequestDto request);

  OrdersResponseDto toResponse(OrdersEntity entity);

  @BeanMapping(nullValuePropertyMappingStrategy = NullValuePropertyMappingStrategy.IGNORE)
  void update(OrdersRequestDto request, @MappingTarget OrdersEntity entity);
}
