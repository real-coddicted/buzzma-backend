package com.coddicted.buzzma.service;

import com.coddicted.buzzma.common.BaseCrudService;
import com.coddicted.buzzma.common.OffsetBasedPageRequest;
import com.coddicted.buzzma.dto.TicketCommentsRequestDto;
import com.coddicted.buzzma.dto.TicketCommentsResponseDto;
import com.coddicted.buzzma.entity.TicketCommentsEntity;
import com.coddicted.buzzma.mapper.TicketCommentsMapper;
import com.coddicted.buzzma.repository.TicketCommentsRepository;
import java.util.List;
import java.util.UUID;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Sort;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
public class TicketCommentsService
    extends BaseCrudService<
        TicketCommentsEntity, TicketCommentsRequestDto, TicketCommentsResponseDto> {
  private final TicketCommentsRepository repository;
  private final TicketCommentsMapper mapper;

  public TicketCommentsService(
      final TicketCommentsRepository repository, final TicketCommentsMapper mapper) {
    this.repository = repository;
    this.mapper = mapper;
  }

  @Transactional(readOnly = true)
  public List<TicketCommentsResponseDto> list(final int limit, final int offset) {
    final Pageable pageable =
        new OffsetBasedPageRequest(limit, offset, Sort.by(Sort.Direction.DESC, "createdAt"));
    return repository.findAll(pageable).getContent().stream().map(mapper::toResponse).toList();
  }

  @Transactional(readOnly = true)
  public TicketCommentsResponseDto getById(final UUID id) {
    final TicketCommentsEntity entity = mustFind(repository, id, "ticket_comments");
    return mapper.toResponse(entity);
  }

  @Transactional
  public TicketCommentsResponseDto create(final TicketCommentsRequestDto request) {
    final TicketCommentsEntity entity = mapper.toEntity(request);
    final TicketCommentsEntity saved = repository.save(entity);
    return mapper.toResponse(saved);
  }

  @Transactional
  public TicketCommentsResponseDto update(final UUID id, final TicketCommentsRequestDto request) {
    final TicketCommentsEntity existing = mustFind(repository, id, "ticket_comments");
    mapper.update(request, existing);
    final TicketCommentsEntity saved = repository.save(existing);
    return mapper.toResponse(saved);
  }

  @Transactional
  public void delete(final UUID id) {
    final TicketCommentsEntity existing = mustFind(repository, id, "ticket_comments");
    existing.setIsDeleted(true);
    repository.save(existing);
  }
}
