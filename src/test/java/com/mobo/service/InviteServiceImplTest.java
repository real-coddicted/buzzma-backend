package com.mobo.service;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

import com.mobo.dto.InvitesRequestDto;
import com.mobo.dto.InvitesResponseDto;
import com.mobo.entity.InvitesEntity;
import com.mobo.exception.ApiException;
import com.mobo.mapper.InvitesMapper;
import com.mobo.repository.InvitesRepository;
import com.mobo.service.impl.InviteServiceImpl;
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
class InviteServiceImplTest {

  @Mock private InvitesRepository repository;
  @Mock private InvitesMapper mapper;
  @InjectMocks private InviteServiceImpl service;

  @Test
  void list_returnsPagedResults() {
    InvitesEntity entity = new InvitesEntity();
    InvitesResponseDto dto = mockInvitesResponse();
    when(repository.findAll(any(Pageable.class))).thenReturn(new PageImpl<>(List.of(entity)));
    when(mapper.toResponse(entity)).thenReturn(dto);

    List<InvitesResponseDto> result = service.list(50, 0);

    assertThat(result).hasSize(1).contains(dto);
  }

  @Test
  void list_returnsEmptyList() {
    when(repository.findAll(any(Pageable.class))).thenReturn(new PageImpl<>(List.of()));

    List<InvitesResponseDto> result = service.list(50, 0);

    assertThat(result).isEmpty();
  }

  @Test
  void getById_found_returnsDto() {
    UUID id = UUID.randomUUID();
    InvitesEntity entity = new InvitesEntity();
    InvitesResponseDto dto = mockInvitesResponse();
    when(repository.findById(id)).thenReturn(Optional.of(entity));
    when(mapper.toResponse(entity)).thenReturn(dto);

    InvitesResponseDto result = service.getById(id);

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
    InvitesRequestDto request = mockInvitesRequest();
    InvitesEntity entity = new InvitesEntity();
    InvitesResponseDto dto = mockInvitesResponse();
    when(mapper.toEntity(request)).thenReturn(entity);
    when(repository.save(entity)).thenReturn(entity);
    when(mapper.toResponse(entity)).thenReturn(dto);

    InvitesResponseDto result = service.create(request);

    verify(repository).save(entity);
    assertThat(result).isEqualTo(dto);
  }

  @Test
  void update_appliesChangesAndReturnsDto() {
    UUID id = UUID.randomUUID();
    InvitesRequestDto request = mockInvitesRequest();
    InvitesEntity entity = new InvitesEntity();
    InvitesResponseDto dto = mockInvitesResponse();
    when(repository.findById(id)).thenReturn(Optional.of(entity));
    when(repository.save(entity)).thenReturn(entity);
    when(mapper.toResponse(entity)).thenReturn(dto);

    InvitesResponseDto result = service.update(id, request);

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

  private InvitesRequestDto mockInvitesRequest() {
    return org.mockito.Mockito.mock(InvitesRequestDto.class);
  }

  private InvitesResponseDto mockInvitesResponse() {
    return org.mockito.Mockito.mock(InvitesResponseDto.class);
  }
}
