package com.mobo.service.impl;

import com.mobo.common.BaseCrudService;
import com.mobo.common.OffsetBasedPageRequest;
import com.mobo.dto.SuspensionsRequestDto;
import com.mobo.dto.SuspensionsResponseDto;
import com.mobo.entity.SuspensionsEntity;
import com.mobo.mapper.SuspensionsMapper;
import com.mobo.repository.SuspensionsRepository;
import com.mobo.service.SuspensionService;
import java.util.List;
import java.util.UUID;
import java.util.stream.Collectors;
import org.springframework.data.domain.Sort;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
public class SuspensionServiceImpl extends BaseCrudService implements SuspensionService {

  private final SuspensionsRepository repository;
  private final SuspensionsMapper mapper;

  public SuspensionServiceImpl(SuspensionsRepository repository, SuspensionsMapper mapper) {
    this.repository = repository;
    this.mapper = mapper;
  }

  @Override
  @Transactional(readOnly = true)
  public List<SuspensionsResponseDto> list(int limit, int offset) {
    var pageable =
        new OffsetBasedPageRequest(limit, offset, Sort.by(Sort.Direction.DESC, "createdAt"));
    return repository.findAll(pageable).stream()
        .map(mapper::toResponse)
        .collect(Collectors.toList());
  }

  @Override
  @Transactional(readOnly = true)
  public SuspensionsResponseDto getById(UUID id) {
    SuspensionsEntity entity = mustFind(repository, id, "Suspensions");
    return mapper.toResponse(entity);
  }

  @Override
  @Transactional
  public SuspensionsResponseDto create(SuspensionsRequestDto request) {
    SuspensionsEntity entity = mapper.toEntity(request);
    return mapper.toResponse(repository.save(entity));
  }

  @Override
  @Transactional
  public SuspensionsResponseDto update(UUID id, SuspensionsRequestDto request) {
    SuspensionsEntity entity = mustFind(repository, id, "Suspensions");
    mapper.update(request, entity);
    return mapper.toResponse(repository.save(entity));
  }

  @Override
  @Transactional
  public void delete(UUID id) {
    repository.deleteById(id);
  }
}
