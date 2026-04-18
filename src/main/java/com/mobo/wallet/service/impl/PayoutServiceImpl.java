package com.mobo.wallet.service.impl;

import com.mobo.shared.common.BaseCrudService;
import com.mobo.shared.common.OffsetBasedPageRequest;
import com.mobo.wallet.api.PayoutsRequestDto;
import com.mobo.wallet.api.PayoutsResponseDto;
import com.mobo.wallet.mapper.PayoutsMapper;
import com.mobo.wallet.persistence.PayoutsEntity;
import com.mobo.wallet.persistence.PayoutsRepository;
import com.mobo.wallet.service.PayoutService;
import java.util.List;
import java.util.UUID;
import java.util.stream.Collectors;
import org.springframework.data.domain.Sort;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
public class PayoutServiceImpl extends BaseCrudService implements PayoutService {

  private final PayoutsRepository repository;
  private final PayoutsMapper mapper;

  public PayoutServiceImpl(PayoutsRepository repository, PayoutsMapper mapper) {
    this.repository = repository;
    this.mapper = mapper;
  }

  @Override
  @Transactional(readOnly = true)
  public List<PayoutsResponseDto> list(int limit, int offset) {
    var pageable =
        new OffsetBasedPageRequest(limit, offset, Sort.by(Sort.Direction.DESC, "createdAt"));
    return repository.findAllByIsDeletedFalse(pageable).stream()
        .map(mapper::toResponse)
        .collect(Collectors.toList());
  }

  @Override
  @Transactional(readOnly = true)
  public PayoutsResponseDto getById(UUID id) {
    PayoutsEntity entity = mustFind(repository, id, "Payouts");
    return mapper.toResponse(entity);
  }

  @Override
  @Transactional
  public PayoutsResponseDto create(PayoutsRequestDto request) {
    PayoutsEntity entity = mapper.toEntity(request);
    return mapper.toResponse(repository.save(entity));
  }

  @Override
  @Transactional
  public PayoutsResponseDto update(UUID id, PayoutsRequestDto request) {
    PayoutsEntity entity = mustFind(repository, id, "Payouts");
    mapper.update(request, entity);
    return mapper.toResponse(repository.save(entity));
  }

  @Override
  @Transactional
  public void delete(UUID id) {
    PayoutsEntity entity = mustFind(repository, id, "Payouts");
    entity.setIsDeleted(true);
    repository.save(entity);
  }
}
