package com.mobo.catalog.service.impl;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

import com.mobo.catalog.api.CampaignsRequestDto;
import com.mobo.catalog.api.CampaignsResponseDto;
import com.mobo.catalog.mapper.CampaignsMapper;
import com.mobo.catalog.persistence.CampaignsEntity;
import com.mobo.catalog.persistence.CampaignsRepository;
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
class CampaignServiceImplTest {

  @Mock private CampaignsRepository repository;
  @Mock private CampaignsMapper mapper;
  @InjectMocks private CampaignServiceImpl service;

  @Test
  void list_returnsPagedResults() {
    CampaignsEntity entity = new CampaignsEntity();
    CampaignsResponseDto dto = mockCampaignsResponse();
    when(repository.findAllByIsDeletedFalse(any(Pageable.class)))
        .thenReturn(new PageImpl<>(List.of(entity)));
    when(mapper.toResponse(entity)).thenReturn(dto);

    List<CampaignsResponseDto> result = service.list(50, 0);

    assertThat(result).hasSize(1).contains(dto);
  }

  @Test
  void list_returnsEmptyList() {
    when(repository.findAllByIsDeletedFalse(any(Pageable.class)))
        .thenReturn(new PageImpl<>(List.of()));

    List<CampaignsResponseDto> result = service.list(50, 0);

    assertThat(result).isEmpty();
  }

  @Test
  void getById_found_returnsDto() {
    UUID id = UUID.randomUUID();
    CampaignsEntity entity = new CampaignsEntity();
    CampaignsResponseDto dto = mockCampaignsResponse();
    when(repository.findById(id)).thenReturn(Optional.of(entity));
    when(mapper.toResponse(entity)).thenReturn(dto);

    CampaignsResponseDto result = service.getById(id);

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
    CampaignsRequestDto request = mockCampaignsRequest();
    CampaignsEntity entity = new CampaignsEntity();
    CampaignsResponseDto dto = mockCampaignsResponse();
    when(mapper.toEntity(request)).thenReturn(entity);
    when(repository.save(entity)).thenReturn(entity);
    when(mapper.toResponse(entity)).thenReturn(dto);

    CampaignsResponseDto result = service.create(request);

    verify(repository).save(entity);
    assertThat(result).isEqualTo(dto);
  }

  @Test
  void update_appliesChangesAndReturnsDto() {
    UUID id = UUID.randomUUID();
    CampaignsRequestDto request = mockCampaignsRequest();
    CampaignsEntity entity = new CampaignsEntity();
    CampaignsResponseDto dto = mockCampaignsResponse();
    when(repository.findById(id)).thenReturn(Optional.of(entity));
    when(repository.save(entity)).thenReturn(entity);
    when(mapper.toResponse(entity)).thenReturn(dto);

    CampaignsResponseDto result = service.update(id, request);

    verify(mapper).update(request, entity);
    verify(repository).save(entity);
    assertThat(result).isEqualTo(dto);
  }

  @Test
  void delete_setsIsDeletedTrue() {
    UUID id = UUID.randomUUID();
    CampaignsEntity entity = new CampaignsEntity();
    when(repository.findById(id)).thenReturn(Optional.of(entity));

    service.delete(id);

    verify(repository, never()).deleteById(any());
    verify(repository).save(entity);
    assertThat(entity.getIsDeleted()).isTrue();
  }

  private CampaignsRequestDto mockCampaignsRequest() {
    return org.mockito.Mockito.mock(CampaignsRequestDto.class);
  }

  private CampaignsResponseDto mockCampaignsResponse() {
    return org.mockito.Mockito.mock(CampaignsResponseDto.class);
  }
}
