package com.coddicted.buzzma.service;

import com.coddicted.buzzma.common.BaseCrudService;
import com.coddicted.buzzma.common.OffsetBasedPageRequest;
import com.coddicted.buzzma.dto.TicketsRequestDto;
import com.coddicted.buzzma.dto.TicketsResponseDto;
import com.coddicted.buzzma.entity.TicketsEntity;
import com.coddicted.buzzma.mapper.TicketsMapper;
import com.coddicted.buzzma.repository.TicketsRepository;
import java.util.List;
import java.util.UUID;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Sort;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
public class TicketsService
    extends BaseCrudService<TicketsEntity, TicketsRequestDto, TicketsResponseDto> {
  private final TicketsRepository repository;
  private final TicketsMapper mapper;

  public TicketsService(final TicketsRepository repository, final TicketsMapper mapper) {
    this.repository = repository;
    this.mapper = mapper;
  }

  @Transactional(readOnly = true)
  public List<TicketsResponseDto> list(final int limit, final int offset) {
    final Pageable pageable =
        new OffsetBasedPageRequest(limit, offset, Sort.by(Sort.Direction.DESC, "createdAt"));
    return repository.findAll(pageable).getContent().stream().map(mapper::toResponse).toList();
  }

  @Transactional(readOnly = true)
  public TicketsResponseDto getById(final UUID id) {
    final TicketsEntity entity = mustFind(repository, id, "tickets");
    return mapper.toResponse(entity);
  }

  @Transactional
  public TicketsResponseDto create(final TicketsRequestDto request) {
    final TicketsEntity entity = mapper.toEntity(request);
    final TicketsEntity saved = repository.save(entity);
    return mapper.toResponse(saved);
  }

  @Transactional
  public TicketsResponseDto update(final UUID id, final TicketsRequestDto request) {
    final TicketsEntity existing = mustFind(repository, id, "tickets");
    mapper.update(request, existing);
    final TicketsEntity saved = repository.save(existing);
    return mapper.toResponse(saved);
  }

  @Transactional
  public void delete(final UUID id) {
    final TicketsEntity existing = mustFind(repository, id, "tickets");
    existing.setIsDeleted(true);
    repository.save(existing);
  }
}
