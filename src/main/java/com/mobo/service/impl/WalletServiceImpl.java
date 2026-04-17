package com.mobo.service.impl;

import com.mobo.common.BaseCrudService;
import com.mobo.common.OffsetBasedPageRequest;
import com.mobo.dto.WalletsRequestDto;
import com.mobo.dto.WalletsResponseDto;
import com.mobo.entity.WalletsEntity;
import com.mobo.mapper.WalletsMapper;
import com.mobo.repository.WalletsRepository;
import com.mobo.service.WalletService;
import java.util.List;
import java.util.UUID;
import java.util.stream.Collectors;
import org.springframework.data.domain.Sort;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
public class WalletServiceImpl extends BaseCrudService implements WalletService {

  private final WalletsRepository repository;
  private final WalletsMapper mapper;

  public WalletServiceImpl(WalletsRepository repository, WalletsMapper mapper) {
    this.repository = repository;
    this.mapper = mapper;
  }

  @Override
  @Transactional(readOnly = true)
  public List<WalletsResponseDto> list(int limit, int offset) {
    var pageable =
        new OffsetBasedPageRequest(limit, offset, Sort.by(Sort.Direction.DESC, "createdAt"));
    return repository.findAllByIsDeletedFalse(pageable).stream()
        .map(mapper::toResponse)
        .collect(Collectors.toList());
  }

  @Override
  @Transactional(readOnly = true)
  public WalletsResponseDto getById(UUID id) {
    WalletsEntity entity = mustFind(repository, id, "Wallets");
    return mapper.toResponse(entity);
  }

  @Override
  @Transactional
  public WalletsResponseDto create(WalletsRequestDto request) {
    WalletsEntity entity = mapper.toEntity(request);
    return mapper.toResponse(repository.save(entity));
  }

  @Override
  @Transactional
  public WalletsResponseDto update(UUID id, WalletsRequestDto request) {
    WalletsEntity entity = mustFind(repository, id, "Wallets");
    mapper.update(request, entity);
    return mapper.toResponse(repository.save(entity));
  }

  @Override
  @Transactional
  public void delete(UUID id) {
    WalletsEntity entity = mustFind(repository, id, "Wallets");
    entity.setIsDeleted(true);
    repository.save(entity);
  }
}
