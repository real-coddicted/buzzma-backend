package com.mobo.mapper;

import com.mobo.dto.OrdersRequestDto;
import com.mobo.dto.OrdersResponseDto;
import com.mobo.entity.OrdersEntity;
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
