package com.coddicted.buzzma.admin.mapper;

import com.coddicted.buzzma.admin.api.AuditLogsRequestDto;
import com.coddicted.buzzma.admin.api.AuditLogsResponseDto;
import com.coddicted.buzzma.admin.persistence.AuditLogsEntity;
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
