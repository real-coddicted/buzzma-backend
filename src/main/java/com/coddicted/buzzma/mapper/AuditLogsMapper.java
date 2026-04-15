package com.coddicted.buzzma.mapper;

import com.coddicted.buzzma.dto.AuditLogsRequestDto;
import com.coddicted.buzzma.dto.AuditLogsResponseDto;
import com.coddicted.buzzma.entity.AuditLogsEntity;
import org.mapstruct.Mapper;
import org.mapstruct.MappingTarget;
import org.mapstruct.NullValuePropertyMappingStrategy;

@Mapper(
    componentModel = "spring",
    nullValuePropertyMappingStrategy = NullValuePropertyMappingStrategy.IGNORE)
public interface AuditLogsMapper {
  AuditLogsEntity toEntity(AuditLogsRequestDto request);

  AuditLogsResponseDto toResponse(AuditLogsEntity entity);

  void update(AuditLogsRequestDto request, @MappingTarget AuditLogsEntity entity);
}
