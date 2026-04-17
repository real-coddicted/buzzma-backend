package com.mobo.service;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

import com.mobo.dto.MediatorProfilesRequestDto;
import com.mobo.dto.MediatorProfilesResponseDto;
import com.mobo.entity.MediatorProfilesEntity;
import com.mobo.exception.ApiException;
import com.mobo.mapper.MediatorProfilesMapper;
import com.mobo.repository.MediatorProfilesRepository;
import com.mobo.service.impl.MediatorProfileServiceImpl;
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
class MediatorProfileServiceImplTest {

  @Mock private MediatorProfilesRepository repository;
  @Mock private MediatorProfilesMapper mapper;
  @InjectMocks private MediatorProfileServiceImpl service;

  @Test
  void list_returnsPagedResults() {
    MediatorProfilesEntity entity = new MediatorProfilesEntity();
    MediatorProfilesResponseDto dto = mockMediatorProfilesResponse();
    when(repository.findAllByIsDeletedFalse(any(Pageable.class)))
        .thenReturn(new PageImpl<>(List.of(entity)));
    when(mapper.toResponse(entity)).thenReturn(dto);

    List<MediatorProfilesResponseDto> result = service.list(50, 0);

    assertThat(result).hasSize(1).contains(dto);
  }

  @Test
  void list_returnsEmptyList() {
    when(repository.findAllByIsDeletedFalse(any(Pageable.class)))
        .thenReturn(new PageImpl<>(List.of()));

    List<MediatorProfilesResponseDto> result = service.list(50, 0);

    assertThat(result).isEmpty();
  }

  @Test
  void getById_found_returnsDto() {
    UUID id = UUID.randomUUID();
    MediatorProfilesEntity entity = new MediatorProfilesEntity();
    MediatorProfilesResponseDto dto = mockMediatorProfilesResponse();
    when(repository.findById(id)).thenReturn(Optional.of(entity));
    when(mapper.toResponse(entity)).thenReturn(dto);

    MediatorProfilesResponseDto result = service.getById(id);

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
    MediatorProfilesRequestDto request = mockMediatorProfilesRequest();
    MediatorProfilesEntity entity = new MediatorProfilesEntity();
    MediatorProfilesResponseDto dto = mockMediatorProfilesResponse();
    when(mapper.toEntity(request)).thenReturn(entity);
    when(repository.save(entity)).thenReturn(entity);
    when(mapper.toResponse(entity)).thenReturn(dto);

    MediatorProfilesResponseDto result = service.create(request);

    verify(repository).save(entity);
    assertThat(result).isEqualTo(dto);
  }

  @Test
  void update_appliesChangesAndReturnsDto() {
    UUID id = UUID.randomUUID();
    MediatorProfilesRequestDto request = mockMediatorProfilesRequest();
    MediatorProfilesEntity entity = new MediatorProfilesEntity();
    MediatorProfilesResponseDto dto = mockMediatorProfilesResponse();
    when(repository.findById(id)).thenReturn(Optional.of(entity));
    when(repository.save(entity)).thenReturn(entity);
    when(mapper.toResponse(entity)).thenReturn(dto);

    MediatorProfilesResponseDto result = service.update(id, request);

    verify(mapper).update(request, entity);
    verify(repository).save(entity);
    assertThat(result).isEqualTo(dto);
  }

  @Test
  void delete_setsIsDeletedTrue() {
    UUID id = UUID.randomUUID();
    MediatorProfilesEntity entity = new MediatorProfilesEntity();
    when(repository.findById(id)).thenReturn(Optional.of(entity));

    service.delete(id);

    verify(repository, never()).deleteById(any());
    verify(repository).save(entity);
    assertThat(entity.getIsDeleted()).isTrue();
  }

  private MediatorProfilesRequestDto mockMediatorProfilesRequest() {
    return org.mockito.Mockito.mock(MediatorProfilesRequestDto.class);
  }

  private MediatorProfilesResponseDto mockMediatorProfilesResponse() {
    return org.mockito.Mockito.mock(MediatorProfilesResponseDto.class);
  }
}
