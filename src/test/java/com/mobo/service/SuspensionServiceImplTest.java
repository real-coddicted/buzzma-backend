package com.mobo.service;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

import com.mobo.dto.SuspensionsRequestDto;
import com.mobo.dto.SuspensionsResponseDto;
import com.mobo.entity.SuspensionsEntity;
import com.mobo.exception.ApiException;
import com.mobo.mapper.SuspensionsMapper;
import com.mobo.repository.SuspensionsRepository;
import com.mobo.service.impl.SuspensionServiceImpl;
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
class SuspensionServiceImplTest {

  @Mock private SuspensionsRepository repository;
  @Mock private SuspensionsMapper mapper;
  @InjectMocks private SuspensionServiceImpl service;

  @Test
  void list_returnsPagedResults() {
    SuspensionsEntity entity = new SuspensionsEntity();
    SuspensionsResponseDto dto = mockSuspensionsResponse();
    when(repository.findAll(any(Pageable.class))).thenReturn(new PageImpl<>(List.of(entity)));
    when(mapper.toResponse(entity)).thenReturn(dto);

    List<SuspensionsResponseDto> result = service.list(50, 0);

    assertThat(result).hasSize(1).contains(dto);
  }

  @Test
  void list_returnsEmptyList() {
    when(repository.findAll(any(Pageable.class))).thenReturn(new PageImpl<>(List.of()));

    List<SuspensionsResponseDto> result = service.list(50, 0);

    assertThat(result).isEmpty();
  }

  @Test
  void getById_found_returnsDto() {
    UUID id = UUID.randomUUID();
    SuspensionsEntity entity = new SuspensionsEntity();
    SuspensionsResponseDto dto = mockSuspensionsResponse();
    when(repository.findById(id)).thenReturn(Optional.of(entity));
    when(mapper.toResponse(entity)).thenReturn(dto);

    SuspensionsResponseDto result = service.getById(id);

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
    SuspensionsRequestDto request = mockSuspensionsRequest();
    SuspensionsEntity entity = new SuspensionsEntity();
    SuspensionsResponseDto dto = mockSuspensionsResponse();
    when(mapper.toEntity(request)).thenReturn(entity);
    when(repository.save(entity)).thenReturn(entity);
    when(mapper.toResponse(entity)).thenReturn(dto);

    SuspensionsResponseDto result = service.create(request);

    verify(repository).save(entity);
    assertThat(result).isEqualTo(dto);
  }

  @Test
  void update_appliesChangesAndReturnsDto() {
    UUID id = UUID.randomUUID();
    SuspensionsRequestDto request = mockSuspensionsRequest();
    SuspensionsEntity entity = new SuspensionsEntity();
    SuspensionsResponseDto dto = mockSuspensionsResponse();
    when(repository.findById(id)).thenReturn(Optional.of(entity));
    when(repository.save(entity)).thenReturn(entity);
    when(mapper.toResponse(entity)).thenReturn(dto);

    SuspensionsResponseDto result = service.update(id, request);

    verify(mapper).update(request, entity);
    verify(repository).save(entity);
    assertThat(result).isEqualTo(dto);
  }

  @Test
  void delete_callsDeleteById() {
    UUID id = UUID.randomUUID();

    service.delete(id);

    verify(repository).deleteById(id);
  }

  private SuspensionsRequestDto mockSuspensionsRequest() {
    return org.mockito.Mockito.mock(SuspensionsRequestDto.class);
  }

  private SuspensionsResponseDto mockSuspensionsResponse() {
    return org.mockito.Mockito.mock(SuspensionsResponseDto.class);
  }
}
