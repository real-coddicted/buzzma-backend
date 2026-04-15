package com.coddicted.buzzma.mapper;

import com.coddicted.buzzma.dto.OrderItemsRequestDto;
import com.coddicted.buzzma.dto.OrderItemsResponseDto;
import com.coddicted.buzzma.entity.OrderItemsEntity;
import org.mapstruct.Mapper;
import org.mapstruct.MappingTarget;
import org.mapstruct.NullValuePropertyMappingStrategy;

@Mapper(
    componentModel = "spring",
    nullValuePropertyMappingStrategy = NullValuePropertyMappingStrategy.IGNORE)
public interface OrderItemsMapper {
  OrderItemsEntity toEntity(OrderItemsRequestDto request);

  OrderItemsResponseDto toResponse(OrderItemsEntity entity);

  void update(OrderItemsRequestDto request, @MappingTarget OrderItemsEntity entity);
}
