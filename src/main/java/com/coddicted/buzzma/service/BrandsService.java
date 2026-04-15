package com.coddicted.buzzma.service;

import com.coddicted.buzzma.common.BaseCrudService;
import com.coddicted.buzzma.common.OffsetBasedPageRequest;
import com.coddicted.buzzma.dto.BrandsRequestDto;
import com.coddicted.buzzma.dto.BrandsResponseDto;
import com.coddicted.buzzma.entity.BrandsEntity;
import com.coddicted.buzzma.mapper.BrandsMapper;
import com.coddicted.buzzma.repository.BrandsRepository;
import java.util.List;
import java.util.UUID;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Sort;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
public class BrandsService
    extends BaseCrudService<BrandsEntity, BrandsRequestDto, BrandsResponseDto> {
  private final BrandsRepository repository;
  private final BrandsMapper mapper;

  public BrandsService(final BrandsRepository repository, final BrandsMapper mapper) {
    this.repository = repository;
    this.mapper = mapper;
  }

  @Transactional(readOnly = true)
  public List<BrandsResponseDto> list(final int limit, final int offset) {
    final Pageable pageable =
        new OffsetBasedPageRequest(limit, offset, Sort.by(Sort.Direction.DESC, "createdAt"));
    return repository.findAll(pageable).getContent().stream().map(mapper::toResponse).toList();
  }

  @Transactional(readOnly = true)
  public BrandsResponseDto getById(final UUID id) {
    final BrandsEntity entity = mustFind(repository, id, "brands");
    return mapper.toResponse(entity);
  }

  @Transactional
  public BrandsResponseDto create(final BrandsRequestDto request) {
    final BrandsEntity entity = mapper.toEntity(request);
    final BrandsEntity saved = repository.save(entity);
    return mapper.toResponse(saved);
  }

  @Transactional
  public BrandsResponseDto update(final UUID id, final BrandsRequestDto request) {
    final BrandsEntity existing = mustFind(repository, id, "brands");
    mapper.update(request, existing);
    final BrandsEntity saved = repository.save(existing);
    return mapper.toResponse(saved);
  }

  @Transactional
  public void delete(final UUID id) {
    final BrandsEntity existing = mustFind(repository, id, "brands");
    existing.setIsDeleted(true);
    repository.save(existing);
  }
}
