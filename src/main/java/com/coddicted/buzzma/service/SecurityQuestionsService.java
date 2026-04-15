package com.coddicted.buzzma.service;

import com.coddicted.buzzma.common.BaseCrudService;
import com.coddicted.buzzma.common.OffsetBasedPageRequest;
import com.coddicted.buzzma.dto.SecurityQuestionsRequestDto;
import com.coddicted.buzzma.dto.SecurityQuestionsResponseDto;
import com.coddicted.buzzma.entity.SecurityQuestionsEntity;
import com.coddicted.buzzma.mapper.SecurityQuestionsMapper;
import com.coddicted.buzzma.repository.SecurityQuestionsRepository;
import java.util.List;
import java.util.UUID;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Sort;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
public class SecurityQuestionsService
    extends BaseCrudService<
        SecurityQuestionsEntity, SecurityQuestionsRequestDto, SecurityQuestionsResponseDto> {
  private final SecurityQuestionsRepository repository;
  private final SecurityQuestionsMapper mapper;

  public SecurityQuestionsService(
      final SecurityQuestionsRepository repository, final SecurityQuestionsMapper mapper) {
    this.repository = repository;
    this.mapper = mapper;
  }

  @Transactional(readOnly = true)
  public List<SecurityQuestionsResponseDto> list(final int limit, final int offset) {
    final Pageable pageable =
        new OffsetBasedPageRequest(limit, offset, Sort.by(Sort.Direction.DESC, "createdAt"));
    return repository.findAll(pageable).getContent().stream().map(mapper::toResponse).toList();
  }

  @Transactional(readOnly = true)
  public SecurityQuestionsResponseDto getById(final UUID id) {
    final SecurityQuestionsEntity entity = mustFind(repository, id, "security_questions");
    return mapper.toResponse(entity);
  }

  @Transactional
  public SecurityQuestionsResponseDto create(final SecurityQuestionsRequestDto request) {
    final SecurityQuestionsEntity entity = mapper.toEntity(request);
    final SecurityQuestionsEntity saved = repository.save(entity);
    return mapper.toResponse(saved);
  }

  @Transactional
  public SecurityQuestionsResponseDto update(
      final UUID id, final SecurityQuestionsRequestDto request) {
    final SecurityQuestionsEntity existing = mustFind(repository, id, "security_questions");
    mapper.update(request, existing);
    final SecurityQuestionsEntity saved = repository.save(existing);
    return mapper.toResponse(saved);
  }

  @Transactional
  public void delete(final UUID id) {
    final SecurityQuestionsEntity existing = mustFind(repository, id, "security_questions");
    repository.delete(existing);
  }
}
