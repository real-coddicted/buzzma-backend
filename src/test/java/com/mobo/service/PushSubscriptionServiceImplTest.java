package com.mobo.service;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

import com.mobo.dto.PushSubscriptionsRequestDto;
import com.mobo.dto.PushSubscriptionsResponseDto;
import com.mobo.entity.PushSubscriptionsEntity;
import com.mobo.exception.ApiException;
import com.mobo.mapper.PushSubscriptionsMapper;
import com.mobo.repository.PushSubscriptionsRepository;
import com.mobo.service.impl.PushSubscriptionServiceImpl;
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
class PushSubscriptionServiceImplTest {

  @Mock private PushSubscriptionsRepository repository;
  @Mock private PushSubscriptionsMapper mapper;
  @InjectMocks private PushSubscriptionServiceImpl service;

  @Test
  void list_returnsPagedResults() {
    PushSubscriptionsEntity entity = new PushSubscriptionsEntity();
    PushSubscriptionsResponseDto dto = mockPushSubscriptionsResponse();
    when(repository.findAllByIsDeletedFalse(any(Pageable.class)))
        .thenReturn(new PageImpl<>(List.of(entity)));
    when(mapper.toResponse(entity)).thenReturn(dto);

    List<PushSubscriptionsResponseDto> result = service.list(50, 0);

    assertThat(result).hasSize(1).contains(dto);
  }

  @Test
  void list_returnsEmptyList() {
    when(repository.findAllByIsDeletedFalse(any(Pageable.class)))
        .thenReturn(new PageImpl<>(List.of()));

    List<PushSubscriptionsResponseDto> result = service.list(50, 0);

    assertThat(result).isEmpty();
  }

  @Test
  void getById_found_returnsDto() {
    UUID id = UUID.randomUUID();
    PushSubscriptionsEntity entity = new PushSubscriptionsEntity();
    PushSubscriptionsResponseDto dto = mockPushSubscriptionsResponse();
    when(repository.findById(id)).thenReturn(Optional.of(entity));
    when(mapper.toResponse(entity)).thenReturn(dto);

    PushSubscriptionsResponseDto result = service.getById(id);

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
    PushSubscriptionsRequestDto request = mockPushSubscriptionsRequest();
    PushSubscriptionsEntity entity = new PushSubscriptionsEntity();
    PushSubscriptionsResponseDto dto = mockPushSubscriptionsResponse();
    when(mapper.toEntity(request)).thenReturn(entity);
    when(repository.save(entity)).thenReturn(entity);
    when(mapper.toResponse(entity)).thenReturn(dto);

    PushSubscriptionsResponseDto result = service.create(request);

    verify(repository).save(entity);
    assertThat(result).isEqualTo(dto);
  }

  @Test
  void update_appliesChangesAndReturnsDto() {
    UUID id = UUID.randomUUID();
    PushSubscriptionsRequestDto request = mockPushSubscriptionsRequest();
    PushSubscriptionsEntity entity = new PushSubscriptionsEntity();
    PushSubscriptionsResponseDto dto = mockPushSubscriptionsResponse();
    when(repository.findById(id)).thenReturn(Optional.of(entity));
    when(repository.save(entity)).thenReturn(entity);
    when(mapper.toResponse(entity)).thenReturn(dto);

    PushSubscriptionsResponseDto result = service.update(id, request);

    verify(mapper).update(request, entity);
    verify(repository).save(entity);
    assertThat(result).isEqualTo(dto);
  }

  @Test
  void delete_setsIsDeletedTrue() {
    UUID id = UUID.randomUUID();
    PushSubscriptionsEntity entity = new PushSubscriptionsEntity();
    when(repository.findById(id)).thenReturn(Optional.of(entity));

    service.delete(id);

    verify(repository, never()).deleteById(any());
    verify(repository).save(entity);
    assertThat(entity.getIsDeleted()).isTrue();
  }

  private PushSubscriptionsRequestDto mockPushSubscriptionsRequest() {
    return org.mockito.Mockito.mock(PushSubscriptionsRequestDto.class);
  }

  private PushSubscriptionsResponseDto mockPushSubscriptionsResponse() {
    return org.mockito.Mockito.mock(PushSubscriptionsResponseDto.class);
  }
}
