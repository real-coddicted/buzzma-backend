package com.mobo.admin.service.impl;

import com.mobo.admin.api.SystemConfigsRequestDto;
import com.mobo.admin.api.SystemConfigsResponseDto;
import com.mobo.admin.mapper.SystemConfigsMapper;
import com.mobo.admin.persistence.SystemConfigsEntity;
import com.mobo.admin.persistence.SystemConfigsRepository;
import com.mobo.admin.service.SystemConfigService;
import com.mobo.shared.common.BaseCrudService;
import com.mobo.shared.common.OffsetBasedPageRequest;
import java.util.List;
import java.util.UUID;
import java.util.stream.Collectors;
import org.springframework.data.domain.Sort;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
public class SystemConfigServiceImpl extends BaseCrudService implements SystemConfigService {

  private final SystemConfigsRepository repository;
  private final SystemConfigsMapper mapper;

  public SystemConfigServiceImpl(SystemConfigsRepository repository, SystemConfigsMapper mapper) {
    this.repository = repository;
    this.mapper = mapper;
  }

  @Override
  @Transactional(readOnly = true)
  public List<SystemConfigsResponseDto> list(int limit, int offset) {
    var pageable =
        new OffsetBasedPageRequest(limit, offset, Sort.by(Sort.Direction.DESC, "createdAt"));
    return repository.findAll(pageable).stream()
        .map(mapper::toResponse)
        .collect(Collectors.toList());
  }

  @Override
  @Transactional(readOnly = true)
  public SystemConfigsResponseDto getById(UUID id) {
    SystemConfigsEntity entity = mustFind(repository, id, "SystemConfigs");
    return mapper.toResponse(entity);
  }

  @Override
  @Transactional
  public SystemConfigsResponseDto create(SystemConfigsRequestDto request) {
    SystemConfigsEntity entity = mapper.toEntity(request);
    return mapper.toResponse(repository.save(entity));
  }

  @Override
  @Transactional
  public SystemConfigsResponseDto update(UUID id, SystemConfigsRequestDto request) {
    SystemConfigsEntity entity = mustFind(repository, id, "SystemConfigs");
    mapper.update(request, entity);
    return mapper.toResponse(repository.save(entity));
  }

  @Override
  @Transactional
  public void delete(UUID id) {
    repository.deleteById(id);
  }
}
