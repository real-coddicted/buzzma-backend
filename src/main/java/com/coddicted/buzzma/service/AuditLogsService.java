package com.coddicted.buzzma.service;

import com.coddicted.buzzma.common.BaseCrudService;
import com.coddicted.buzzma.common.OffsetBasedPageRequest;
import com.coddicted.buzzma.dto.AuditLogsRequestDto;
import com.coddicted.buzzma.dto.AuditLogsResponseDto;
import com.coddicted.buzzma.entity.AuditLogsEntity;
import com.coddicted.buzzma.mapper.AuditLogsMapper;
import com.coddicted.buzzma.repository.AuditLogsRepository;
import java.util.List;
import java.util.UUID;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Sort;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
public class AuditLogsService
    extends BaseCrudService<AuditLogsEntity, AuditLogsRequestDto, AuditLogsResponseDto> {
  private final AuditLogsRepository repository;
  private final AuditLogsMapper mapper;

  public AuditLogsService(final AuditLogsRepository repository, final AuditLogsMapper mapper) {
    this.repository = repository;
    this.mapper = mapper;
  }

  @Transactional(readOnly = true)
  public List<AuditLogsResponseDto> list(final int limit, final int offset) {
    final Pageable pageable =
        new OffsetBasedPageRequest(limit, offset, Sort.by(Sort.Direction.DESC, "createdAt"));
    return repository.findAll(pageable).getContent().stream().map(mapper::toResponse).toList();
  }

  @Transactional(readOnly = true)
  public AuditLogsResponseDto getById(final UUID id) {
    final AuditLogsEntity entity = mustFind(repository, id, "audit_logs");
    return mapper.toResponse(entity);
  }

  @Transactional
  public AuditLogsResponseDto create(final AuditLogsRequestDto request) {
    final AuditLogsEntity entity = mapper.toEntity(request);
    final AuditLogsEntity saved = repository.save(entity);
    return mapper.toResponse(saved);
  }

  @Transactional
  public AuditLogsResponseDto update(final UUID id, final AuditLogsRequestDto request) {
    final AuditLogsEntity existing = mustFind(repository, id, "audit_logs");
    mapper.update(request, existing);
    final AuditLogsEntity saved = repository.save(existing);
    return mapper.toResponse(saved);
  }

  @Transactional
  public void delete(final UUID id) {
    final AuditLogsEntity existing = mustFind(repository, id, "audit_logs");
    repository.delete(existing);
  }
}
