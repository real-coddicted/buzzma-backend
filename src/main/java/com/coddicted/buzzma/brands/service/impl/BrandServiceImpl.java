package com.coddicted.buzzma.brands.service.impl;

import com.coddicted.buzzma.brands.api.BrandsRequestDto;
import com.coddicted.buzzma.brands.api.BrandsResponseDto;
import com.coddicted.buzzma.brands.mapper.BrandsMapper;
import com.coddicted.buzzma.brands.persistence.BrandsEntity;
import com.coddicted.buzzma.brands.persistence.BrandsRepository;
import com.coddicted.buzzma.brands.service.BrandService;
import com.coddicted.buzzma.shared.common.BaseCrudService;
import com.coddicted.buzzma.shared.common.OffsetBasedPageRequest;
import java.util.List;
import java.util.UUID;
import java.util.stream.Collectors;
import org.springframework.data.domain.Sort;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
public class BrandServiceImpl extends BaseCrudService implements BrandService {

  private final BrandsRepository repository;
  private final BrandsMapper mapper;

  public BrandServiceImpl(BrandsRepository repository, BrandsMapper mapper) {
    this.repository = repository;
    this.mapper = mapper;
  }

  @Override
  @Transactional(readOnly = true)
  public List<BrandsResponseDto> list(int limit, int offset) {
    var pageable =
        new OffsetBasedPageRequest(limit, offset, Sort.by(Sort.Direction.DESC, "createdAt"));
    return repository.findAllByIsDeletedFalse(pageable).stream()
        .map(mapper::toResponse)
        .collect(Collectors.toList());
  }

  @Override
  @Transactional(readOnly = true)
  public BrandsResponseDto getById(UUID id) {
    BrandsEntity entity = mustFind(repository, id, "Brands");
    return mapper.toResponse(entity);
  }

  @Override
  @Transactional
  public BrandsResponseDto create(BrandsRequestDto request) {
    BrandsEntity entity = mapper.toEntity(request);
    return mapper.toResponse(repository.save(entity));
  }

  @Override
  @Transactional
  public BrandsResponseDto update(UUID id, BrandsRequestDto request) {
    BrandsEntity entity = mustFind(repository, id, "Brands");
    mapper.update(request, entity);
    return mapper.toResponse(repository.save(entity));
  }

  @Override
  @Transactional
  public void delete(UUID id) {
    BrandsEntity entity = mustFind(repository, id, "Brands");
    entity.setIsDeleted(true);
    repository.save(entity);
  }
}
