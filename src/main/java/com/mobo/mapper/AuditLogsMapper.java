package com.mobo.mapper;

import com.mobo.dto.AuditLogsRequestDto;
import com.mobo.dto.AuditLogsResponseDto;
import com.mobo.entity.AuditLogsEntity;
import org.mapstruct.BeanMapping;
import org.mapstruct.Mapper;
import org.mapstruct.MappingTarget;
import org.mapstruct.NullValuePropertyMappingStrategy;

@Mapper(
    componentModel = "spring",
    nullValuePropertyMappingStrategy = NullValuePropertyMappingStrategy.IGNORE)
public interface AuditLogsMapper {

  AuditLogsEntity toEntity(AuditLogsRequestDto request);

  AuditLogsResponseDto toResponse(AuditLogsEntity entity);

  @BeanMapping(nullValuePropertyMappingStrategy = NullValuePropertyMappingStrategy.IGNORE)
  void update(AuditLogsRequestDto request, @MappingTarget AuditLogsEntity entity);
}
