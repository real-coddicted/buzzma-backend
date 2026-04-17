package com.mobo.service.impl;

import com.mobo.common.BaseCrudService;
import com.mobo.common.OffsetBasedPageRequest;
import com.mobo.dto.MediatorProfilesRequestDto;
import com.mobo.dto.MediatorProfilesResponseDto;
import com.mobo.entity.MediatorProfilesEntity;
import com.mobo.mapper.MediatorProfilesMapper;
import com.mobo.repository.MediatorProfilesRepository;
import com.mobo.service.MediatorProfileService;
import java.util.List;
import java.util.UUID;
import java.util.stream.Collectors;
import org.springframework.data.domain.Sort;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
public class MediatorProfileServiceImpl extends BaseCrudService implements MediatorProfileService {

  private final MediatorProfilesRepository repository;
  private final MediatorProfilesMapper mapper;

  public MediatorProfileServiceImpl(
      MediatorProfilesRepository repository, MediatorProfilesMapper mapper) {
    this.repository = repository;
    this.mapper = mapper;
  }

  @Override
  @Transactional(readOnly = true)
  public List<MediatorProfilesResponseDto> list(int limit, int offset) {
    var pageable =
        new OffsetBasedPageRequest(limit, offset, Sort.by(Sort.Direction.DESC, "createdAt"));
    return repository.findAllByIsDeletedFalse(pageable).stream()
        .map(mapper::toResponse)
        .collect(Collectors.toList());
  }

  @Override
  @Transactional(readOnly = true)
  public MediatorProfilesResponseDto getById(UUID id) {
    MediatorProfilesEntity entity = mustFind(repository, id, "MediatorProfiles");
    return mapper.toResponse(entity);
  }

  @Override
  @Transactional
  public MediatorProfilesResponseDto create(MediatorProfilesRequestDto request) {
    MediatorProfilesEntity entity = mapper.toEntity(request);
    return mapper.toResponse(repository.save(entity));
  }

  @Override
  @Transactional
  public MediatorProfilesResponseDto update(UUID id, MediatorProfilesRequestDto request) {
    MediatorProfilesEntity entity = mustFind(repository, id, "MediatorProfiles");
    mapper.update(request, entity);
    return mapper.toResponse(repository.save(entity));
  }

  @Override
  @Transactional
  public void delete(UUID id) {
    MediatorProfilesEntity entity = mustFind(repository, id, "MediatorProfiles");
    entity.setIsDeleted(true);
    repository.save(entity);
  }
}
