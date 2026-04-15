package com.coddicted.buzzma.service;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

import com.coddicted.buzzma.dto.UsersRequestDto;
import com.coddicted.buzzma.dto.UsersResponseDto;
import com.coddicted.buzzma.entity.UsersEntity;
import com.coddicted.buzzma.mapper.UsersMapper;
import com.coddicted.buzzma.repository.UsersRepository;
import java.util.List;
import java.util.Optional;
import java.util.UUID;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.data.domain.PageImpl;
import org.springframework.data.domain.Pageable;
import org.springframework.web.server.ResponseStatusException;

@ExtendWith(MockitoExtension.class)
class UsersServiceTest {
  @Mock private UsersRepository repository;
  @Mock private UsersMapper mapper;
  @InjectMocks private UsersService service;
  private UUID id;
  private UsersEntity entity;
  private UsersRequestDto requestDto;
  private UsersResponseDto responseDto;

  @BeforeEach
  void setUp() {
    id = UUID.randomUUID();
    entity = new UsersEntity();
    entity.setId(id);
    requestDto = new UsersRequestDto();
    responseDto = new UsersResponseDto();
    responseDto.setId(id);
  }

  @Test
  void listShouldReturnMappedResults() {
    when(repository.findAll(any(Pageable.class))).thenReturn(new PageImpl<>(List.of(entity)));
    when(mapper.toResponse(entity)).thenReturn(responseDto);
    final List<UsersResponseDto> result = service.list(10, 0);
    assertThat(result).hasSize(1);
    assertThat(result.getFirst().getId()).isEqualTo(id);
    verify(repository).findAll(any(Pageable.class));
  }

  @Test
  void listShouldReturnEmptyWhenNoResults() {
    when(repository.findAll(any(Pageable.class))).thenReturn(new PageImpl<>(List.of()));
    final List<UsersResponseDto> result = service.list(10, 0);
    assertThat(result).isEmpty();
  }

  @Test
  void getByIdShouldReturnDto() {
    when(repository.findById(id)).thenReturn(Optional.of(entity));
    when(mapper.toResponse(entity)).thenReturn(responseDto);
    final UsersResponseDto result = service.getById(id);
    assertThat(result.getId()).isEqualTo(id);
  }

  @Test
  void getByIdShouldThrowWhenNotFound() {
    when(repository.findById(id)).thenReturn(Optional.empty());
    assertThatThrownBy(() -> service.getById(id))
        .isInstanceOf(ResponseStatusException.class)
        .hasMessageContaining("users");
  }

  @Test
  void createShouldSaveAndReturnDto() {
    when(mapper.toEntity(requestDto)).thenReturn(entity);
    when(repository.save(entity)).thenReturn(entity);
    when(mapper.toResponse(entity)).thenReturn(responseDto);
    final UsersResponseDto result = service.create(requestDto);
    assertThat(result.getId()).isEqualTo(id);
    verify(repository).save(entity);
  }

  @Test
  void updateShouldModifyAndReturnDto() {
    when(repository.findById(id)).thenReturn(Optional.of(entity));
    when(repository.save(entity)).thenReturn(entity);
    when(mapper.toResponse(entity)).thenReturn(responseDto);
    final UsersResponseDto result = service.update(id, requestDto);
    assertThat(result.getId()).isEqualTo(id);
    verify(mapper).update(requestDto, entity);
    verify(repository).save(entity);
  }

  @Test
  void updateShouldThrowWhenNotFound() {
    when(repository.findById(id)).thenReturn(Optional.empty());
    assertThatThrownBy(() -> service.update(id, requestDto))
        .isInstanceOf(ResponseStatusException.class)
        .hasMessageContaining("users");
  }

  @Test
  void deleteShouldRemoveEntity() {
    when(repository.findById(id)).thenReturn(Optional.of(entity));
    service.delete(id);
    assertThat(entity.getIsDeleted()).isTrue();
    verify(repository).save(entity);
  }

  @Test
  void deleteShouldThrowWhenNotFound() {
    when(repository.findById(id)).thenReturn(Optional.empty());
    assertThatThrownBy(() -> service.delete(id))
        .isInstanceOf(ResponseStatusException.class)
        .hasMessageContaining("users");
  }
}
