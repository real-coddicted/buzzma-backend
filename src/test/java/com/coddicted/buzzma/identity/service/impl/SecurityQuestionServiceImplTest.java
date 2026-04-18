package com.coddicted.buzzma.identity.service.impl;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

import com.coddicted.buzzma.identity.api.SecurityQuestionsRequestDto;
import com.coddicted.buzzma.identity.api.SecurityQuestionsResponseDto;
import com.coddicted.buzzma.identity.mapper.SecurityQuestionsMapper;
import com.coddicted.buzzma.identity.persistence.SecurityQuestionsEntity;
import com.coddicted.buzzma.identity.persistence.SecurityQuestionsRepository;
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
class SecurityQuestionServiceImplTest {

  @Mock private SecurityQuestionsRepository repository;
  @Mock private SecurityQuestionsMapper mapper;
  @InjectMocks private SecurityQuestionServiceImpl service;

  @Test
  void list_returnsPagedResults() {
    SecurityQuestionsEntity entity = new SecurityQuestionsEntity();
    SecurityQuestionsResponseDto dto = mockSecurityQuestionsResponse();
    when(repository.findAll(any(Pageable.class))).thenReturn(new PageImpl<>(List.of(entity)));
    when(mapper.toResponse(entity)).thenReturn(dto);

    List<SecurityQuestionsResponseDto> result = service.list(50, 0);

    assertThat(result).hasSize(1).contains(dto);
  }

  @Test
  void list_returnsEmptyList() {
    when(repository.findAll(any(Pageable.class))).thenReturn(new PageImpl<>(List.of()));

    List<SecurityQuestionsResponseDto> result = service.list(50, 0);

    assertThat(result).isEmpty();
  }

  @Test
  void getById_found_returnsDto() {
    UUID id = UUID.randomUUID();
    SecurityQuestionsEntity entity = new SecurityQuestionsEntity();
    SecurityQuestionsResponseDto dto = mockSecurityQuestionsResponse();
    when(repository.findById(id)).thenReturn(Optional.of(entity));
    when(mapper.toResponse(entity)).thenReturn(dto);

    SecurityQuestionsResponseDto result = service.getById(id);

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
    SecurityQuestionsRequestDto request = mockSecurityQuestionsRequest();
    SecurityQuestionsEntity entity = new SecurityQuestionsEntity();
    SecurityQuestionsResponseDto dto = mockSecurityQuestionsResponse();
    when(mapper.toEntity(request)).thenReturn(entity);
    when(repository.save(entity)).thenReturn(entity);
    when(mapper.toResponse(entity)).thenReturn(dto);

    SecurityQuestionsResponseDto result = service.create(request);

    verify(repository).save(entity);
    assertThat(result).isEqualTo(dto);
  }

  @Test
  void update_appliesChangesAndReturnsDto() {
    UUID id = UUID.randomUUID();
    SecurityQuestionsRequestDto request = mockSecurityQuestionsRequest();
    SecurityQuestionsEntity entity = new SecurityQuestionsEntity();
    SecurityQuestionsResponseDto dto = mockSecurityQuestionsResponse();
    when(repository.findById(id)).thenReturn(Optional.of(entity));
    when(repository.save(entity)).thenReturn(entity);
    when(mapper.toResponse(entity)).thenReturn(dto);

    SecurityQuestionsResponseDto result = service.update(id, request);

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

  private SecurityQuestionsRequestDto mockSecurityQuestionsRequest() {
    return org.mockito.Mockito.mock(SecurityQuestionsRequestDto.class);
  }

  private SecurityQuestionsResponseDto mockSecurityQuestionsResponse() {
    return org.mockito.Mockito.mock(SecurityQuestionsResponseDto.class);
  }
}
