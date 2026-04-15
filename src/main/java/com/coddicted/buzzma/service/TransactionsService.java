package com.coddicted.buzzma.service;

import com.coddicted.buzzma.common.BaseCrudService;
import com.coddicted.buzzma.common.OffsetBasedPageRequest;
import com.coddicted.buzzma.dto.TransactionsRequestDto;
import com.coddicted.buzzma.dto.TransactionsResponseDto;
import com.coddicted.buzzma.entity.TransactionsEntity;
import com.coddicted.buzzma.mapper.TransactionsMapper;
import com.coddicted.buzzma.repository.TransactionsRepository;
import java.util.List;
import java.util.UUID;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Sort;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
public class TransactionsService
    extends BaseCrudService<TransactionsEntity, TransactionsRequestDto, TransactionsResponseDto> {
  private final TransactionsRepository repository;
  private final TransactionsMapper mapper;

  public TransactionsService(
      final TransactionsRepository repository, final TransactionsMapper mapper) {
    this.repository = repository;
    this.mapper = mapper;
  }

  @Transactional(readOnly = true)
  public List<TransactionsResponseDto> list(final int limit, final int offset) {
    final Pageable pageable =
        new OffsetBasedPageRequest(limit, offset, Sort.by(Sort.Direction.DESC, "createdAt"));
    return repository.findAll(pageable).getContent().stream().map(mapper::toResponse).toList();
  }

  @Transactional(readOnly = true)
  public TransactionsResponseDto getById(final UUID id) {
    final TransactionsEntity entity = mustFind(repository, id, "transactions");
    return mapper.toResponse(entity);
  }

  @Transactional
  public TransactionsResponseDto create(final TransactionsRequestDto request) {
    final TransactionsEntity entity = mapper.toEntity(request);
    final TransactionsEntity saved = repository.save(entity);
    return mapper.toResponse(saved);
  }

  @Transactional
  public TransactionsResponseDto update(final UUID id, final TransactionsRequestDto request) {
    final TransactionsEntity existing = mustFind(repository, id, "transactions");
    mapper.update(request, existing);
    final TransactionsEntity saved = repository.save(existing);
    return mapper.toResponse(saved);
  }

  @Transactional
  public void delete(final UUID id) {
    final TransactionsEntity existing = mustFind(repository, id, "transactions");
    existing.setIsDeleted(true);
    repository.save(existing);
  }
}
