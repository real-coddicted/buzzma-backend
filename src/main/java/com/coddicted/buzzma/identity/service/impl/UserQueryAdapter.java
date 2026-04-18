package com.coddicted.buzzma.identity.service.impl;

import com.coddicted.buzzma.identity.api.UserQueryPort;
import com.coddicted.buzzma.identity.api.UsersResponseDto;
import com.coddicted.buzzma.identity.mapper.UsersMapper;
import com.coddicted.buzzma.identity.persistence.UsersEntity;
import com.coddicted.buzzma.identity.persistence.UsersRepository;
import com.coddicted.buzzma.shared.common.OffsetBasedPageRequest;
import com.coddicted.buzzma.shared.enums.UserStatus;
import java.util.List;
import java.util.Optional;
import java.util.UUID;
import java.util.stream.Collectors;
import org.springframework.data.domain.Sort;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
public class UserQueryAdapter implements UserQueryPort {

  private final UsersRepository usersRepository;
  private final UsersMapper usersMapper;

  public UserQueryAdapter(UsersRepository usersRepository, UsersMapper usersMapper) {
    this.usersRepository = usersRepository;
    this.usersMapper = usersMapper;
  }

  @Override
  @Transactional(readOnly = true)
  public Optional<UsersResponseDto> findById(UUID id) {
    return usersRepository
        .findById(id)
        .filter(u -> !Boolean.TRUE.equals(u.getIsDeleted()))
        .map(usersMapper::toResponse);
  }

  @Override
  @Transactional(readOnly = true)
  public Optional<UsersResponseDto> findByMediatorCode(String mediatorCode) {
    return usersRepository
        .findByMediatorCodeAndIsDeletedFalse(mediatorCode)
        .map(usersMapper::toResponse);
  }

  @Override
  @Transactional(readOnly = true)
  public Optional<String> findBrandCodeById(UUID id) {
    return usersRepository
        .findById(id)
        .filter(u -> !Boolean.TRUE.equals(u.getIsDeleted()))
        .map(UsersEntity::getBrandCode);
  }

  @Override
  @Transactional(readOnly = true)
  public Optional<UserStatus> findStatusById(UUID id) {
    return usersRepository
        .findById(id)
        .filter(u -> !Boolean.TRUE.equals(u.getIsDeleted()))
        .map(UsersEntity::getStatus);
  }

  @Override
  @Transactional(readOnly = true)
  public String[] findConnectedAgenciesById(UUID id) {
    return usersRepository
        .findById(id)
        .filter(u -> !Boolean.TRUE.equals(u.getIsDeleted()))
        .map(UsersEntity::getConnectedAgencies)
        .orElse(new String[0]);
  }

  @Override
  @Transactional(readOnly = true)
  public List<UsersResponseDto> listAll(int limit, int offset) {
    var pageable =
        new OffsetBasedPageRequest(limit, offset, Sort.by(Sort.Direction.DESC, "createdAt"));
    return usersRepository.findAllByIsDeletedFalse(pageable).stream()
        .map(usersMapper::toResponse)
        .collect(Collectors.toList());
  }

  @Override
  @Transactional(readOnly = true)
  public List<UsersResponseDto> listByParentCode(String parentCode, int limit, int offset) {
    return usersRepository.findAllByParentCodeAndIsDeletedFalse(parentCode).stream()
        .map(usersMapper::toResponse)
        .collect(Collectors.toList());
  }

  @Override
  @Transactional(readOnly = true)
  public List<UsersResponseDto> listByParentCodeAndVerified(
      String parentCode, boolean verified, int limit, int offset) {
    var pageable =
        new OffsetBasedPageRequest(limit, offset, Sort.by(Sort.Direction.DESC, "createdAt"));
    return usersRepository
        .findAllByParentCodeAndIsVerifiedByMediatorAndIsDeletedFalse(parentCode, verified, pageable)
        .stream()
        .map(usersMapper::toResponse)
        .collect(Collectors.toList());
  }

  @Override
  @Transactional(readOnly = true)
  public long count() {
    return usersRepository.count();
  }
}
