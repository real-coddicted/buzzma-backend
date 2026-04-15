package com.coddicted.buzzma.service;

import com.coddicted.buzzma.common.BaseCrudService;
import com.coddicted.buzzma.common.OffsetBasedPageRequest;
import com.coddicted.buzzma.dto.UsersRequestDto;
import com.coddicted.buzzma.dto.UsersResponseDto;
import com.coddicted.buzzma.entity.UsersEntity;
import com.coddicted.buzzma.mapper.UsersMapper;
import com.coddicted.buzzma.repository.UsersRepository;
import java.util.List;
import java.util.UUID;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Sort;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
public class UsersService extends BaseCrudService<UsersEntity, UsersRequestDto, UsersResponseDto> {
  private final UsersRepository repository;
  private final UsersMapper mapper;

  public UsersService(final UsersRepository repository, final UsersMapper mapper) {
    this.repository = repository;
    this.mapper = mapper;
  }

  @Transactional(readOnly = true)
  public List<UsersResponseDto> list(final int limit, final int offset) {
    final Pageable pageable =
        new OffsetBasedPageRequest(limit, offset, Sort.by(Sort.Direction.DESC, "createdAt"));
    return repository.findAll(pageable).getContent().stream().map(mapper::toResponse).toList();
  }

  @Transactional(readOnly = true)
  public UsersResponseDto getById(final UUID id) {
    final UsersEntity entity = mustFind(repository, id, "users");
    return mapper.toResponse(entity);
  }

  @Transactional
  public UsersResponseDto create(final UsersRequestDto request) {
    final UsersEntity entity = mapper.toEntity(request);
    final UsersEntity saved = repository.save(entity);
    return mapper.toResponse(saved);
  }

  @Transactional
  public UsersResponseDto update(final UUID id, final UsersRequestDto request) {
    final UsersEntity existing = mustFind(repository, id, "users");
    mapper.update(request, existing);
    final UsersEntity saved = repository.save(existing);
    return mapper.toResponse(saved);
  }

  @Transactional
  public void delete(final UUID id) {
    final UsersEntity existing = mustFind(repository, id, "users");
    existing.setIsDeleted(true);
    repository.save(existing);
  }
}
