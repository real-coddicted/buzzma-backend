package com.coddicted.buzzma.service;

import com.coddicted.buzzma.common.BaseCrudService;
import com.coddicted.buzzma.common.OffsetBasedPageRequest;
import com.coddicted.buzzma.dto.ShopperProfilesRequestDto;
import com.coddicted.buzzma.dto.ShopperProfilesResponseDto;
import com.coddicted.buzzma.entity.ShopperProfilesEntity;
import com.coddicted.buzzma.mapper.ShopperProfilesMapper;
import com.coddicted.buzzma.repository.ShopperProfilesRepository;
import java.util.List;
import java.util.UUID;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Sort;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
public class ShopperProfilesService
    extends BaseCrudService<
        ShopperProfilesEntity, ShopperProfilesRequestDto, ShopperProfilesResponseDto> {
  private final ShopperProfilesRepository repository;
  private final ShopperProfilesMapper mapper;

  public ShopperProfilesService(
      final ShopperProfilesRepository repository, final ShopperProfilesMapper mapper) {
    this.repository = repository;
    this.mapper = mapper;
  }

  @Transactional(readOnly = true)
  public List<ShopperProfilesResponseDto> list(final int limit, final int offset) {
    final Pageable pageable =
        new OffsetBasedPageRequest(limit, offset, Sort.by(Sort.Direction.DESC, "createdAt"));
    return repository.findAll(pageable).getContent().stream().map(mapper::toResponse).toList();
  }

  @Transactional(readOnly = true)
  public ShopperProfilesResponseDto getById(final UUID id) {
    final ShopperProfilesEntity entity = mustFind(repository, id, "shopper_profiles");
    return mapper.toResponse(entity);
  }

  @Transactional
  public ShopperProfilesResponseDto create(final ShopperProfilesRequestDto request) {
    final ShopperProfilesEntity entity = mapper.toEntity(request);
    final ShopperProfilesEntity saved = repository.save(entity);
    return mapper.toResponse(saved);
  }

  @Transactional
  public ShopperProfilesResponseDto update(final UUID id, final ShopperProfilesRequestDto request) {
    final ShopperProfilesEntity existing = mustFind(repository, id, "shopper_profiles");
    mapper.update(request, existing);
    final ShopperProfilesEntity saved = repository.save(existing);
    return mapper.toResponse(saved);
  }

  @Transactional
  public void delete(final UUID id) {
    final ShopperProfilesEntity existing = mustFind(repository, id, "shopper_profiles");
    existing.setIsDeleted(true);
    repository.save(existing);
  }
}
