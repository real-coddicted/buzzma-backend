package com.mobo.mapper;

import com.mobo.dto.OrderItemsRequestDto;
import com.mobo.dto.OrderItemsResponseDto;
import com.mobo.entity.OrderItemsEntity;
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
