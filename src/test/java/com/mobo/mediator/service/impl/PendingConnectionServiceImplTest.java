package com.mobo.mediator.service.impl;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

import com.mobo.mediator.api.PendingConnectionsRequestDto;
import com.mobo.mediator.api.PendingConnectionsResponseDto;
import com.mobo.mediator.mapper.PendingConnectionsMapper;
import com.mobo.mediator.persistence.PendingConnectionsEntity;
import com.mobo.mediator.persistence.PendingConnectionsRepository;
import com.mobo.shared.exception.ApiException;
import java.util.List;
import java.util.Optional;
import java.util.UUID;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.data.domain.PageImpl;
import org.springframework.data.domain.Pageable;
import org.springframework.http.HttpStatus;

@ExtendWith(MockitoExtension.class)
class PendingConnectionServiceImplTest {

  @Mock private PendingConnectionsRepository repository;
  @Mock private PendingConnectionsMapper mapper;
  @InjectMocks private PendingConnectionServiceImpl service;

  @Test
  void list_returnsPagedResults() {
    PendingConnectionsEntity entity = new PendingConnectionsEntity();
    PendingConnectionsResponseDto dto = mockPendingConnectionsResponse();
    when(repository.findAllByIsDeletedFalse(any(Pageable.class)))
        .thenReturn(new PageImpl<>(List.of(entity)));
    when(mapper.toResponse(entity)).thenReturn(dto);

    List<PendingConnectionsResponseDto> result = service.list(50, 0);

    assertThat(result).hasSize(1).contains(dto);
  }

  @Test
  void list_returnsEmptyList() {
    when(repository.findAllByIsDeletedFalse(any(Pageable.class)))
        .thenReturn(new PageImpl<>(List.of()));

    List<PendingConnectionsResponseDto> result = service.list(50, 0);

    assertThat(result).isEmpty();
  }

  @Test
  void getById_found_returnsDto() {
    UUID id = UUID.randomUUID();
    PendingConnectionsEntity entity = new PendingConnectionsEntity();
    PendingConnectionsResponseDto dto = mockPendingConnectionsResponse();
    when(repository.findById(id)).thenReturn(Optional.of(entity));
    when(mapper.toResponse(entity)).thenReturn(dto);

    PendingConnectionsResponseDto result = service.getById(id);

    assertThat(result).isEqualTo(dto);
  }

  @Test
  void getById_notFound_throwsNotFound() {
    UUID id = UUID.randomUUID();
    when(repository.findById(id)).thenReturn(Optional.empty());

    assertThatThrownBy(() -> service.getById(id))
        .isInstanceOf(ApiException.class)
        .satisfies(
            ex -> assertThat(((ApiException) ex).getStatus()).isEqualTo(HttpStatus.NOT_FOUND));
  }

  @Test
  void create_savesEntityAndReturnsDto() {
    PendingConnectionsRequestDto request = mockPendingConnectionsRequest();
    PendingConnectionsEntity entity = new PendingConnectionsEntity();
    PendingConnectionsResponseDto dto = mockPendingConnectionsResponse();
    when(mapper.toEntity(request)).thenReturn(entity);
    when(repository.save(entity)).thenReturn(entity);
    when(mapper.toResponse(entity)).thenReturn(dto);

    PendingConnectionsResponseDto result = service.create(request);

    verify(repository).save(entity);
    assertThat(result).isEqualTo(dto);
  }

  @Test
  void update_appliesChangesAndReturnsDto() {
    UUID id = UUID.randomUUID();
    PendingConnectionsRequestDto request = mockPendingConnectionsRequest();
    PendingConnectionsEntity entity = new PendingConnectionsEntity();
    PendingConnectionsResponseDto dto = mockPendingConnectionsResponse();
    when(repository.findById(id)).thenReturn(Optional.of(entity));
    when(repository.save(entity)).thenReturn(entity);
    when(mapper.toResponse(entity)).thenReturn(dto);

    PendingConnectionsResponseDto result = service.update(id, request);

    verify(mapper).update(request, entity);
    verify(repository).save(entity);
    assertThat(result).isEqualTo(dto);
  }

  @Test
  void delete_setsIsDeletedTrue() {
    UUID id = UUID.randomUUID();
    PendingConnectionsEntity entity = new PendingConnectionsEntity();
    when(repository.findById(id)).thenReturn(Optional.of(entity));

    service.delete(id);

    verify(repository, never()).deleteById(any());
    verify(repository).save(entity);
    assertThat(entity.getIsDeleted()).isTrue();
  }

  private PendingConnectionsRequestDto mockPendingConnectionsRequest() {
    return org.mockito.Mockito.mock(PendingConnectionsRequestDto.class);
  }

  private PendingConnectionsResponseDto mockPendingConnectionsResponse() {
    return org.mockito.Mockito.mock(PendingConnectionsResponseDto.class);
  }
}
