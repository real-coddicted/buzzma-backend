package com.mobo.service.impl;

import com.mobo.common.BaseCrudService;
import com.mobo.common.OffsetBasedPageRequest;
import com.mobo.dto.AgenciesRequestDto;
import com.mobo.dto.AgenciesResponseDto;
import com.mobo.entity.AgenciesEntity;
import com.mobo.mapper.AgenciesMapper;
import com.mobo.repository.AgenciesRepository;
import com.mobo.service.AgencyService;
import java.util.List;
import java.util.UUID;
import java.util.stream.Collectors;
import org.springframework.data.domain.Sort;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
public class AgencyServiceImpl extends BaseCrudService implements AgencyService {

  private final AgenciesRepository repository;
  private final AgenciesMapper mapper;

  public AgencyServiceImpl(AgenciesRepository repository, AgenciesMapper mapper) {
    this.repository = repository;
    this.mapper = mapper;
  }

  @Override
  @Transactional(readOnly = true)
  public List<AgenciesResponseDto> list(int limit, int offset) {
    var pageable =
        new OffsetBasedPageRequest(limit, offset, Sort.by(Sort.Direction.DESC, "createdAt"));
    return repository.findAllByIsDeletedFalse(pageable).stream()
        .map(mapper::toResponse)
        .collect(Collectors.toList());
  }

  @Override
  @Transactional(readOnly = true)
  public AgenciesResponseDto getById(UUID id) {
    AgenciesEntity entity = mustFind(repository, id, "Agencies");
    return mapper.toResponse(entity);
  }

  @Override
  @Transactional
  public AgenciesResponseDto create(AgenciesRequestDto request) {
    AgenciesEntity entity = mapper.toEntity(request);
    return mapper.toResponse(repository.save(entity));
  }

  @Override
  @Transactional
  public AgenciesResponseDto update(UUID id, AgenciesRequestDto request) {
    AgenciesEntity entity = mustFind(repository, id, "Agencies");
    mapper.update(request, entity);
    return mapper.toResponse(repository.save(entity));
  }

  @Override
  @Transactional
  public void delete(UUID id) {
    AgenciesEntity entity = mustFind(repository, id, "Agencies");
    entity.setIsDeleted(true);
    repository.save(entity);
  }
}
