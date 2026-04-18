package com.mobo.wallet.service.impl;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

import com.mobo.shared.exception.ApiException;
import com.mobo.wallet.api.PayoutsRequestDto;
import com.mobo.wallet.api.PayoutsResponseDto;
import com.mobo.wallet.mapper.PayoutsMapper;
import com.mobo.wallet.persistence.PayoutsEntity;
import com.mobo.wallet.persistence.PayoutsRepository;
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
class PayoutServiceImplTest {

  @Mock private PayoutsRepository repository;
  @Mock private PayoutsMapper mapper;
  @InjectMocks private PayoutServiceImpl service;

  @Test
  void list_returnsPagedResults() {
    PayoutsEntity entity = new PayoutsEntity();
    PayoutsResponseDto dto = mockPayoutsResponse();
    when(repository.findAllByIsDeletedFalse(any(Pageable.class)))
        .thenReturn(new PageImpl<>(List.of(entity)));
    when(mapper.toResponse(entity)).thenReturn(dto);

    List<PayoutsResponseDto> result = service.list(50, 0);

    assertThat(result).hasSize(1).contains(dto);
  }

  @Test
  void list_returnsEmptyList() {
    when(repository.findAllByIsDeletedFalse(any(Pageable.class)))
        .thenReturn(new PageImpl<>(List.of()));

    List<PayoutsResponseDto> result = service.list(50, 0);

    assertThat(result).isEmpty();
  }

  @Test
  void getById_found_returnsDto() {
    UUID id = UUID.randomUUID();
    PayoutsEntity entity = new PayoutsEntity();
    PayoutsResponseDto dto = mockPayoutsResponse();
    when(repository.findById(id)).thenReturn(Optional.of(entity));
    when(mapper.toResponse(entity)).thenReturn(dto);

    PayoutsResponseDto result = service.getById(id);

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
    PayoutsRequestDto request = mockPayoutsRequest();
    PayoutsEntity entity = new PayoutsEntity();
    PayoutsResponseDto dto = mockPayoutsResponse();
    when(mapper.toEntity(request)).thenReturn(entity);
    when(repository.save(entity)).thenReturn(entity);
    when(mapper.toResponse(entity)).thenReturn(dto);

    PayoutsResponseDto result = service.create(request);

    verify(repository).save(entity);
    assertThat(result).isEqualTo(dto);
  }

  @Test
  void update_appliesChangesAndReturnsDto() {
    UUID id = UUID.randomUUID();
    PayoutsRequestDto request = mockPayoutsRequest();
    PayoutsEntity entity = new PayoutsEntity();
    PayoutsResponseDto dto = mockPayoutsResponse();
    when(repository.findById(id)).thenReturn(Optional.of(entity));
    when(repository.save(entity)).thenReturn(entity);
    when(mapper.toResponse(entity)).thenReturn(dto);

    PayoutsResponseDto result = service.update(id, request);

    verify(mapper).update(request, entity);
    verify(repository).save(entity);
    assertThat(result).isEqualTo(dto);
  }

  @Test
  void delete_setsIsDeletedTrue() {
    UUID id = UUID.randomUUID();
    PayoutsEntity entity = new PayoutsEntity();
    when(repository.findById(id)).thenReturn(Optional.of(entity));

    service.delete(id);

    verify(repository, never()).deleteById(any());
    verify(repository).save(entity);
    assertThat(entity.getIsDeleted()).isTrue();
  }

  private PayoutsRequestDto mockPayoutsRequest() {
    return org.mockito.Mockito.mock(PayoutsRequestDto.class);
  }

  private PayoutsResponseDto mockPayoutsResponse() {
    return org.mockito.Mockito.mock(PayoutsResponseDto.class);
  }
}
