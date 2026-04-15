package com.coddicted.buzzma.service;

import com.coddicted.buzzma.common.BaseCrudService;
import com.coddicted.buzzma.common.OffsetBasedPageRequest;
import com.coddicted.buzzma.dto.WalletsRequestDto;
import com.coddicted.buzzma.dto.WalletsResponseDto;
import com.coddicted.buzzma.entity.WalletsEntity;
import com.coddicted.buzzma.mapper.WalletsMapper;
import com.coddicted.buzzma.repository.WalletsRepository;
import java.util.List;
import java.util.UUID;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Sort;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
public class WalletsService
    extends BaseCrudService<WalletsEntity, WalletsRequestDto, WalletsResponseDto> {
  private final WalletsRepository repository;
  private final WalletsMapper mapper;

  public WalletsService(final WalletsRepository repository, final WalletsMapper mapper) {
    this.repository = repository;
    this.mapper = mapper;
  }

  @Transactional(readOnly = true)
  public List<WalletsResponseDto> list(final int limit, final int offset) {
    final Pageable pageable =
        new OffsetBasedPageRequest(limit, offset, Sort.by(Sort.Direction.DESC, "createdAt"));
    return repository.findAll(pageable).getContent().stream().map(mapper::toResponse).toList();
  }

  @Transactional(readOnly = true)
  public WalletsResponseDto getById(final UUID id) {
    final WalletsEntity entity = mustFind(repository, id, "wallets");
    return mapper.toResponse(entity);
  }

  @Transactional
  public WalletsResponseDto create(final WalletsRequestDto request) {
    final WalletsEntity entity = mapper.toEntity(request);
    final WalletsEntity saved = repository.save(entity);
    return mapper.toResponse(saved);
  }

  @Transactional
  public WalletsResponseDto update(final UUID id, final WalletsRequestDto request) {
    final WalletsEntity existing = mustFind(repository, id, "wallets");
    mapper.update(request, existing);
    final WalletsEntity saved = repository.save(existing);
    return mapper.toResponse(saved);
  }

  @Transactional
  public void delete(final UUID id) {
    final WalletsEntity existing = mustFind(repository, id, "wallets");
    existing.setIsDeleted(true);
    repository.save(existing);
  }
}
