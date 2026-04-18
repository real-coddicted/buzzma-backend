package com.mobo.identity.service.impl;

import com.mobo.identity.api.UsersRequestDto;
import com.mobo.identity.api.UsersResponseDto;
import com.mobo.identity.mapper.UsersMapper;
import com.mobo.identity.persistence.UsersEntity;
import com.mobo.identity.persistence.UsersRepository;
import com.mobo.identity.service.UserService;
import com.mobo.shared.common.BaseCrudService;
import com.mobo.shared.common.OffsetBasedPageRequest;
import java.util.List;
import java.util.UUID;
import java.util.stream.Collectors;
import org.springframework.data.domain.Sort;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
public class UserServiceImpl extends BaseCrudService implements UserService {

  private final UsersRepository repository;
  private final UsersMapper mapper;

  public UserServiceImpl(UsersRepository repository, UsersMapper mapper) {
    this.repository = repository;
    this.mapper = mapper;
  }

  @Override
  @Transactional(readOnly = true)
  public List<UsersResponseDto> list(int limit, int offset) {
    var pageable =
        new OffsetBasedPageRequest(limit, offset, Sort.by(Sort.Direction.DESC, "createdAt"));
    return repository.findAllByIsDeletedFalse(pageable).stream()
        .map(mapper::toResponse)
        .collect(Collectors.toList());
  }

  @Override
  @Transactional(readOnly = true)
  public UsersResponseDto getById(UUID id) {
    UsersEntity entity = mustFind(repository, id, "Users");
    return mapper.toResponse(entity);
  }

  @Override
  @Transactional
  public UsersResponseDto create(UsersRequestDto request) {
    UsersEntity entity = mapper.toEntity(request);
    return mapper.toResponse(repository.save(entity));
  }

  @Override
  @Transactional
  public UsersResponseDto update(UUID id, UsersRequestDto request) {
    UsersEntity entity = mustFind(repository, id, "Users");
    mapper.update(request, entity);
    return mapper.toResponse(repository.save(entity));
  }

  @Override
  @Transactional
  public void delete(UUID id) {
    UsersEntity entity = mustFind(repository, id, "Users");
    entity.setIsDeleted(true);
    repository.save(entity);
  }
}
