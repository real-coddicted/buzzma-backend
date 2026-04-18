package com.mobo.wallet.service.impl;

import com.mobo.shared.common.BaseCrudService;
import com.mobo.shared.common.OffsetBasedPageRequest;
import com.mobo.wallet.api.TransactionsRequestDto;
import com.mobo.wallet.api.TransactionsResponseDto;
import com.mobo.wallet.mapper.TransactionsMapper;
import com.mobo.wallet.persistence.TransactionsEntity;
import com.mobo.wallet.persistence.TransactionsRepository;
import com.mobo.wallet.service.TransactionService;
import java.util.List;
import java.util.UUID;
import java.util.stream.Collectors;
import org.springframework.data.domain.Sort;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
public class TransactionServiceImpl extends BaseCrudService implements TransactionService {

  private final TransactionsRepository repository;
  private final TransactionsMapper mapper;

  public TransactionServiceImpl(TransactionsRepository repository, TransactionsMapper mapper) {
    this.repository = repository;
    this.mapper = mapper;
  }

  @Override
  @Transactional(readOnly = true)
  public List<TransactionsResponseDto> list(int limit, int offset) {
    var pageable =
        new OffsetBasedPageRequest(limit, offset, Sort.by(Sort.Direction.DESC, "createdAt"));
    return repository.findAllByIsDeletedFalse(pageable).stream()
        .map(mapper::toResponse)
        .collect(Collectors.toList());
  }

  @Override
  @Transactional(readOnly = true)
  public TransactionsResponseDto getById(UUID id) {
    TransactionsEntity entity = mustFind(repository, id, "Transactions");
    return mapper.toResponse(entity);
  }

  @Override
  @Transactional
  public TransactionsResponseDto create(TransactionsRequestDto request) {
    TransactionsEntity entity = mapper.toEntity(request);
    return mapper.toResponse(repository.save(entity));
  }

  @Override
  @Transactional
  public TransactionsResponseDto update(UUID id, TransactionsRequestDto request) {
    TransactionsEntity entity = mustFind(repository, id, "Transactions");
    mapper.update(request, entity);
    return mapper.toResponse(repository.save(entity));
  }

  @Override
  @Transactional
  public void delete(UUID id) {
    TransactionsEntity entity = mustFind(repository, id, "Transactions");
    entity.setIsDeleted(true);
    repository.save(entity);
  }
}
