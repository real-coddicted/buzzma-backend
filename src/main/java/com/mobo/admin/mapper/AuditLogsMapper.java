package com.mobo.admin.mapper;

import com.mobo.admin.api.AuditLogsRequestDto;
import com.mobo.admin.api.AuditLogsResponseDto;
import com.mobo.admin.persistence.AuditLogsEntity;
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
