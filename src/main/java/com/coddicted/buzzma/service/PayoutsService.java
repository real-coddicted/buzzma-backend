package com.coddicted.buzzma.service;

import com.coddicted.buzzma.common.BaseCrudService;
import com.coddicted.buzzma.common.OffsetBasedPageRequest;
import com.coddicted.buzzma.dto.PayoutsRequestDto;
import com.coddicted.buzzma.dto.PayoutsResponseDto;
import com.coddicted.buzzma.entity.PayoutsEntity;
import com.coddicted.buzzma.mapper.PayoutsMapper;
import com.coddicted.buzzma.repository.PayoutsRepository;
import java.util.List;
import java.util.UUID;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Sort;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
public class PayoutsService
    extends BaseCrudService<PayoutsEntity, PayoutsRequestDto, PayoutsResponseDto> {
  private final PayoutsRepository repository;
  private final PayoutsMapper mapper;

  public PayoutsService(final PayoutsRepository repository, final PayoutsMapper mapper) {
    this.repository = repository;
    this.mapper = mapper;
  }

  @Transactional(readOnly = true)
  public List<PayoutsResponseDto> list(final int limit, final int offset) {
    final Pageable pageable =
        new OffsetBasedPageRequest(limit, offset, Sort.by(Sort.Direction.DESC, "createdAt"));
    return repository.findAll(pageable).getContent().stream().map(mapper::toResponse).toList();
  }

  @Transactional(readOnly = true)
  public PayoutsResponseDto getById(final UUID id) {
    final PayoutsEntity entity = mustFind(repository, id, "payouts");
    return mapper.toResponse(entity);
  }

  @Transactional
  public PayoutsResponseDto create(final PayoutsRequestDto request) {
    final PayoutsEntity entity = mapper.toEntity(request);
    final PayoutsEntity saved = repository.save(entity);
    return mapper.toResponse(saved);
  }

  @Transactional
  public PayoutsResponseDto update(final UUID id, final PayoutsRequestDto request) {
    final PayoutsEntity existing = mustFind(repository, id, "payouts");
    mapper.update(request, existing);
    final PayoutsEntity saved = repository.save(existing);
    return mapper.toResponse(saved);
  }

  @Transactional
  public void delete(final UUID id) {
    final PayoutsEntity existing = mustFind(repository, id, "payouts");
    existing.setIsDeleted(true);
    repository.save(existing);
  }
}
