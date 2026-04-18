package com.mobo.orders.mapper;

import com.mobo.orders.api.OrderItemsRequestDto;
import com.mobo.orders.api.OrderItemsResponseDto;
import com.mobo.orders.persistence.OrderItemsEntity;
import org.mapstruct.BeanMapping;
import org.mapstruct.Mapper;
import org.mapstruct.MappingTarget;
import org.mapstruct.NullValuePropertyMappingStrategy;

@Mapper(
    componentModel = "spring",
    nullValuePropertyMappingStrategy = NullValuePropertyMappingStrategy.IGNORE)
public interface OrderItemsMapper {

  OrderItemsEntity toEntity(OrderItemsRequestDto request);

  OrderItemsResponseDto toResponse(OrderItemsEntity entity);

  @BeanMapping(nullValuePropertyMappingStrategy = NullValuePropertyMappingStrategy.IGNORE)
  void update(OrderItemsRequestDto request, @MappingTarget OrderItemsEntity entity);
}
