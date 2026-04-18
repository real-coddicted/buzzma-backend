package com.coddicted.buzzma.buyers.service.impl;

import com.coddicted.buzzma.buyers.api.ShopperProfilesRequestDto;
import com.coddicted.buzzma.buyers.api.ShopperProfilesResponseDto;
import com.coddicted.buzzma.buyers.mapper.ShopperProfilesMapper;
import com.coddicted.buzzma.buyers.persistence.ShopperProfilesEntity;
import com.coddicted.buzzma.buyers.persistence.ShopperProfilesRepository;
import com.coddicted.buzzma.buyers.service.ShopperProfileService;
import com.coddicted.buzzma.shared.common.BaseCrudService;
import com.coddicted.buzzma.shared.common.OffsetBasedPageRequest;
import java.util.List;
import java.util.UUID;
import java.util.stream.Collectors;
import org.springframework.data.domain.Sort;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
public class ShopperProfileServiceImpl extends BaseCrudService implements ShopperProfileService {

  private final ShopperProfilesRepository repository;
  private final ShopperProfilesMapper mapper;

  public ShopperProfileServiceImpl(
      ShopperProfilesRepository repository, ShopperProfilesMapper mapper) {
    this.repository = repository;
    this.mapper = mapper;
  }

  @Override
  @Transactional(readOnly = true)
  public List<ShopperProfilesResponseDto> list(int limit, int offset) {
    var pageable =
        new OffsetBasedPageRequest(limit, offset, Sort.by(Sort.Direction.DESC, "createdAt"));
    return repository.findAllByIsDeletedFalse(pageable).stream()
        .map(mapper::toResponse)
        .collect(Collectors.toList());
  }

  @Override
  @Transactional(readOnly = true)
  public ShopperProfilesResponseDto getById(UUID id) {
    ShopperProfilesEntity entity = mustFind(repository, id, "ShopperProfiles");
    return mapper.toResponse(entity);
  }

  @Override
  @Transactional
  public ShopperProfilesResponseDto create(ShopperProfilesRequestDto request) {
    ShopperProfilesEntity entity = mapper.toEntity(request);
    return mapper.toResponse(repository.save(entity));
  }

  @Override
  @Transactional
  public ShopperProfilesResponseDto update(UUID id, ShopperProfilesRequestDto request) {
    ShopperProfilesEntity entity = mustFind(repository, id, "ShopperProfiles");
    mapper.update(request, entity);
    return mapper.toResponse(repository.save(entity));
  }

  @Override
  @Transactional
  public void delete(UUID id) {
    ShopperProfilesEntity entity = mustFind(repository, id, "ShopperProfiles");
    entity.setIsDeleted(true);
    repository.save(entity);
  }
}
