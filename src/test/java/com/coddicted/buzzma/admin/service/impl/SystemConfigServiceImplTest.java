package com.coddicted.buzzma.admin.service.impl;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

import com.coddicted.buzzma.admin.api.SystemConfigsRequestDto;
import com.coddicted.buzzma.admin.api.SystemConfigsResponseDto;
import com.coddicted.buzzma.admin.mapper.SystemConfigsMapper;
import com.coddicted.buzzma.admin.persistence.SystemConfigsEntity;
import com.coddicted.buzzma.admin.persistence.SystemConfigsRepository;
import com.coddicted.buzzma.shared.exception.ApiException;
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
class SystemConfigServiceImplTest {

  @Mock private SystemConfigsRepository repository;
  @Mock private SystemConfigsMapper mapper;
  @InjectMocks private SystemConfigServiceImpl service;

  @Test
  void list_returnsPagedResults() {
    SystemConfigsEntity entity = new SystemConfigsEntity();
    SystemConfigsResponseDto dto = mockSystemConfigsResponse();
    when(repository.findAll(any(Pageable.class))).thenReturn(new PageImpl<>(List.of(entity)));
    when(mapper.toResponse(entity)).thenReturn(dto);

    List<SystemConfigsResponseDto> result = service.list(50, 0);

    assertThat(result).hasSize(1).contains(dto);
  }

  @Test
  void list_returnsEmptyList() {
    when(repository.findAll(any(Pageable.class))).thenReturn(new PageImpl<>(List.of()));

    List<SystemConfigsResponseDto> result = service.list(50, 0);

    assertThat(result).isEmpty();
  }

  @Test
  void getById_found_returnsDto() {
    UUID id = UUID.randomUUID();
    SystemConfigsEntity entity = new SystemConfigsEntity();
    SystemConfigsResponseDto dto = mockSystemConfigsResponse();
    when(repository.findById(id)).thenReturn(Optional.of(entity));
    when(mapper.toResponse(entity)).thenReturn(dto);

    SystemConfigsResponseDto result = service.getById(id);

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
    SystemConfigsRequestDto request = mockSystemConfigsRequest();
    SystemConfigsEntity entity = new SystemConfigsEntity();
    SystemConfigsResponseDto dto = mockSystemConfigsResponse();
    when(mapper.toEntity(request)).thenReturn(entity);
    when(repository.save(entity)).thenReturn(entity);
    when(mapper.toResponse(entity)).thenReturn(dto);

    SystemConfigsResponseDto result = service.create(request);

    verify(repository).save(entity);
    assertThat(result).isEqualTo(dto);
  }

  @Test
  void update_appliesChangesAndReturnsDto() {
    UUID id = UUID.randomUUID();
    SystemConfigsRequestDto request = mockSystemConfigsRequest();
    SystemConfigsEntity entity = new SystemConfigsEntity();
    SystemConfigsResponseDto dto = mockSystemConfigsResponse();
    when(repository.findById(id)).thenReturn(Optional.of(entity));
    when(repository.save(entity)).thenReturn(entity);
    when(mapper.toResponse(entity)).thenReturn(dto);

    SystemConfigsResponseDto result = service.update(id, request);

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

  private SystemConfigsRequestDto mockSystemConfigsRequest() {
    return org.mockito.Mockito.mock(SystemConfigsRequestDto.class);
  }

  private SystemConfigsResponseDto mockSystemConfigsResponse() {
    return org.mockito.Mockito.mock(SystemConfigsResponseDto.class);
  }
}
