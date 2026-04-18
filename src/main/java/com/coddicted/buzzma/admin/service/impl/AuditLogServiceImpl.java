package com.coddicted.buzzma.admin.service.impl;

import com.coddicted.buzzma.admin.api.AuditLogsRequestDto;
import com.coddicted.buzzma.admin.api.AuditLogsResponseDto;
import com.coddicted.buzzma.admin.mapper.AuditLogsMapper;
import com.coddicted.buzzma.admin.persistence.AuditLogsEntity;
import com.coddicted.buzzma.admin.persistence.AuditLogsRepository;
import com.coddicted.buzzma.admin.service.AuditLogService;
import com.coddicted.buzzma.shared.common.BaseCrudService;
import com.coddicted.buzzma.shared.common.OffsetBasedPageRequest;
import java.util.List;
import java.util.UUID;
import java.util.stream.Collectors;
import org.springframework.data.domain.Sort;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
public class AuditLogServiceImpl extends BaseCrudService implements AuditLogService {

  private final AuditLogsRepository repository;
  private final AuditLogsMapper mapper;

  public AuditLogServiceImpl(AuditLogsRepository repository, AuditLogsMapper mapper) {
    this.repository = repository;
    this.mapper = mapper;
  }

  @Override
  @Transactional(readOnly = true)
  public List<AuditLogsResponseDto> list(int limit, int offset) {
    var pageable =
        new OffsetBasedPageRequest(limit, offset, Sort.by(Sort.Direction.DESC, "createdAt"));
    return repository.findAll(pageable).stream()
        .map(mapper::toResponse)
        .collect(Collectors.toList());
  }

  @Override
  @Transactional(readOnly = true)
  public AuditLogsResponseDto getById(UUID id) {
    AuditLogsEntity entity = mustFind(repository, id, "AuditLogs");
    return mapper.toResponse(entity);
  }

  @Override
  @Transactional
  public AuditLogsResponseDto create(AuditLogsRequestDto request) {
    AuditLogsEntity entity = mapper.toEntity(request);
    return mapper.toResponse(repository.save(entity));
  }

  @Override
  @Transactional
  public AuditLogsResponseDto update(UUID id, AuditLogsRequestDto request) {
    AuditLogsEntity entity = mustFind(repository, id, "AuditLogs");
    mapper.update(request, entity);
    return mapper.toResponse(repository.save(entity));
  }

  @Override
  @Transactional
  public void delete(UUID id) {
    repository.deleteById(id);
  }
}
