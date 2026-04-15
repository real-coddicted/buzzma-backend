package com.coddicted.buzzma.service;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

import com.coddicted.buzzma.dto.WalletsRequestDto;
import com.coddicted.buzzma.dto.WalletsResponseDto;
import com.coddicted.buzzma.entity.WalletsEntity;
import com.coddicted.buzzma.mapper.WalletsMapper;
import com.coddicted.buzzma.repository.WalletsRepository;
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
class WalletsServiceTest {
  @Mock private WalletsRepository repository;
  @Mock private WalletsMapper mapper;
  @InjectMocks private WalletsService service;
  private UUID id;
  private WalletsEntity entity;
  private WalletsRequestDto requestDto;
  private WalletsResponseDto responseDto;

  @BeforeEach
  void setUp() {
    id = UUID.randomUUID();
    entity = new WalletsEntity();
    entity.setId(id);
    requestDto = new WalletsRequestDto();
    responseDto = new WalletsResponseDto();
    responseDto.setId(id);
  }

  @Test
  void listShouldReturnMappedResults() {
    when(repository.findAll(any(Pageable.class))).thenReturn(new PageImpl<>(List.of(entity)));
    when(mapper.toResponse(entity)).thenReturn(responseDto);
    final List<WalletsResponseDto> result = service.list(10, 0);
    assertThat(result).hasSize(1);
    assertThat(result.getFirst().getId()).isEqualTo(id);
    verify(repository).findAll(any(Pageable.class));
  }

  @Test
  void listShouldReturnEmptyWhenNoResults() {
    when(repository.findAll(any(Pageable.class))).thenReturn(new PageImpl<>(List.of()));
    final List<WalletsResponseDto> result = service.list(10, 0);
    assertThat(result).isEmpty();
  }

  @Test
  void getByIdShouldReturnDto() {
    when(repository.findById(id)).thenReturn(Optional.of(entity));
    when(mapper.toResponse(entity)).thenReturn(responseDto);
    final WalletsResponseDto result = service.getById(id);
    assertThat(result.getId()).isEqualTo(id);
  }

  @Test
  void getByIdShouldThrowWhenNotFound() {
    when(repository.findById(id)).thenReturn(Optional.empty());
    assertThatThrownBy(() -> service.getById(id))
        .isInstanceOf(ResponseStatusException.class)
        .hasMessageContaining("wallets");
  }

  @Test
  void createShouldSaveAndReturnDto() {
    when(mapper.toEntity(requestDto)).thenReturn(entity);
    when(repository.save(entity)).thenReturn(entity);
    when(mapper.toResponse(entity)).thenReturn(responseDto);
    final WalletsResponseDto result = service.create(requestDto);
    assertThat(result.getId()).isEqualTo(id);
    verify(repository).save(entity);
  }

  @Test
  void updateShouldModifyAndReturnDto() {
    when(repository.findById(id)).thenReturn(Optional.of(entity));
    when(repository.save(entity)).thenReturn(entity);
    when(mapper.toResponse(entity)).thenReturn(responseDto);
    final WalletsResponseDto result = service.update(id, requestDto);
    assertThat(result.getId()).isEqualTo(id);
    verify(mapper).update(requestDto, entity);
    verify(repository).save(entity);
  }

  @Test
  void updateShouldThrowWhenNotFound() {
    when(repository.findById(id)).thenReturn(Optional.empty());
    assertThatThrownBy(() -> service.update(id, requestDto))
        .isInstanceOf(ResponseStatusException.class)
        .hasMessageContaining("wallets");
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
        .hasMessageContaining("wallets");
  }
}
