package com.mobo.support.service.impl;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

import com.mobo.shared.exception.ApiException;
import com.mobo.support.api.TicketCommentsRequestDto;
import com.mobo.support.api.TicketCommentsResponseDto;
import com.mobo.support.mapper.TicketCommentsMapper;
import com.mobo.support.persistence.TicketCommentsEntity;
import com.mobo.support.persistence.TicketCommentsRepository;
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
class TicketCommentServiceImplTest {

  @Mock private TicketCommentsRepository repository;
  @Mock private TicketCommentsMapper mapper;
  @InjectMocks private TicketCommentServiceImpl service;

  @Test
  void list_returnsPagedResults() {
    TicketCommentsEntity entity = new TicketCommentsEntity();
    TicketCommentsResponseDto dto = mockTicketCommentsResponse();
    when(repository.findAll(any(Pageable.class))).thenReturn(new PageImpl<>(List.of(entity)));
    when(mapper.toResponse(entity)).thenReturn(dto);

    List<TicketCommentsResponseDto> result = service.list(50, 0);

    assertThat(result).hasSize(1).contains(dto);
  }

  @Test
  void list_returnsEmptyList() {
    when(repository.findAll(any(Pageable.class))).thenReturn(new PageImpl<>(List.of()));

    List<TicketCommentsResponseDto> result = service.list(50, 0);

    assertThat(result).isEmpty();
  }

  @Test
  void getById_found_returnsDto() {
    UUID id = UUID.randomUUID();
    TicketCommentsEntity entity = new TicketCommentsEntity();
    TicketCommentsResponseDto dto = mockTicketCommentsResponse();
    when(repository.findById(id)).thenReturn(Optional.of(entity));
    when(mapper.toResponse(entity)).thenReturn(dto);

    TicketCommentsResponseDto result = service.getById(id);

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
    TicketCommentsRequestDto request = mockTicketCommentsRequest();
    TicketCommentsEntity entity = new TicketCommentsEntity();
    TicketCommentsResponseDto dto = mockTicketCommentsResponse();
    when(mapper.toEntity(request)).thenReturn(entity);
    when(repository.save(entity)).thenReturn(entity);
    when(mapper.toResponse(entity)).thenReturn(dto);

    TicketCommentsResponseDto result = service.create(request);

    verify(repository).save(entity);
    assertThat(result).isEqualTo(dto);
  }

  @Test
  void update_appliesChangesAndReturnsDto() {
    UUID id = UUID.randomUUID();
    TicketCommentsRequestDto request = mockTicketCommentsRequest();
    TicketCommentsEntity entity = new TicketCommentsEntity();
    TicketCommentsResponseDto dto = mockTicketCommentsResponse();
    when(repository.findById(id)).thenReturn(Optional.of(entity));
    when(repository.save(entity)).thenReturn(entity);
    when(mapper.toResponse(entity)).thenReturn(dto);

    TicketCommentsResponseDto result = service.update(id, request);

    verify(mapper).update(request, entity);
    verify(repository).save(entity);
    assertThat(result).isEqualTo(dto);
  }

  @Test
  void delete_setsIsDeletedTrue() {
    UUID id = UUID.randomUUID();
    TicketCommentsEntity entity = new TicketCommentsEntity();
    when(repository.findById(id)).thenReturn(Optional.of(entity));

    service.delete(id);

    verify(repository, never()).deleteById(any());
    verify(repository).save(entity);
    assertThat(entity.getIsDeleted()).isTrue();
  }

  private TicketCommentsRequestDto mockTicketCommentsRequest() {
    return org.mockito.Mockito.mock(TicketCommentsRequestDto.class);
  }

  private TicketCommentsResponseDto mockTicketCommentsResponse() {
    return org.mockito.Mockito.mock(TicketCommentsResponseDto.class);
  }
}
