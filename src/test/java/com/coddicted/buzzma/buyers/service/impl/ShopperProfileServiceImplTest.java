package com.coddicted.buzzma.buyers.service.impl;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

import com.coddicted.buzzma.buyers.api.ShopperProfilesRequestDto;
import com.coddicted.buzzma.buyers.api.ShopperProfilesResponseDto;
import com.coddicted.buzzma.buyers.mapper.ShopperProfilesMapper;
import com.coddicted.buzzma.buyers.persistence.ShopperProfilesEntity;
import com.coddicted.buzzma.buyers.persistence.ShopperProfilesRepository;
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
class ShopperProfileServiceImplTest {

  @Mock private ShopperProfilesRepository repository;
  @Mock private ShopperProfilesMapper mapper;
  @InjectMocks private ShopperProfileServiceImpl service;

  @Test
  void list_returnsPagedResults() {
    ShopperProfilesEntity entity = new ShopperProfilesEntity();
    ShopperProfilesResponseDto dto = mockShopperProfilesResponse();
    when(repository.findAllByIsDeletedFalse(any(Pageable.class)))
        .thenReturn(new PageImpl<>(List.of(entity)));
    when(mapper.toResponse(entity)).thenReturn(dto);

    List<ShopperProfilesResponseDto> result = service.list(50, 0);

    assertThat(result).hasSize(1).contains(dto);
  }

  @Test
  void list_returnsEmptyList() {
    when(repository.findAllByIsDeletedFalse(any(Pageable.class)))
        .thenReturn(new PageImpl<>(List.of()));

    List<ShopperProfilesResponseDto> result = service.list(50, 0);

    assertThat(result).isEmpty();
  }

  @Test
  void getById_found_returnsDto() {
    UUID id = UUID.randomUUID();
    ShopperProfilesEntity entity = new ShopperProfilesEntity();
    ShopperProfilesResponseDto dto = mockShopperProfilesResponse();
    when(repository.findById(id)).thenReturn(Optional.of(entity));
    when(mapper.toResponse(entity)).thenReturn(dto);

    ShopperProfilesResponseDto result = service.getById(id);

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
    ShopperProfilesRequestDto request = mockShopperProfilesRequest();
    ShopperProfilesEntity entity = new ShopperProfilesEntity();
    ShopperProfilesResponseDto dto = mockShopperProfilesResponse();
    when(mapper.toEntity(request)).thenReturn(entity);
    when(repository.save(entity)).thenReturn(entity);
    when(mapper.toResponse(entity)).thenReturn(dto);

    ShopperProfilesResponseDto result = service.create(request);

    verify(repository).save(entity);
    assertThat(result).isEqualTo(dto);
  }

  @Test
  void update_appliesChangesAndReturnsDto() {
    UUID id = UUID.randomUUID();
    ShopperProfilesRequestDto request = mockShopperProfilesRequest();
    ShopperProfilesEntity entity = new ShopperProfilesEntity();
    ShopperProfilesResponseDto dto = mockShopperProfilesResponse();
    when(repository.findById(id)).thenReturn(Optional.of(entity));
    when(repository.save(entity)).thenReturn(entity);
    when(mapper.toResponse(entity)).thenReturn(dto);

    ShopperProfilesResponseDto result = service.update(id, request);

    verify(mapper).update(request, entity);
    verify(repository).save(entity);
    assertThat(result).isEqualTo(dto);
  }

  @Test
  void delete_setsIsDeletedTrue() {
    UUID id = UUID.randomUUID();
    ShopperProfilesEntity entity = new ShopperProfilesEntity();
    when(repository.findById(id)).thenReturn(Optional.of(entity));

    service.delete(id);

    verify(repository, never()).deleteById(any());
    verify(repository).save(entity);
    assertThat(entity.getIsDeleted()).isTrue();
  }

  private ShopperProfilesRequestDto mockShopperProfilesRequest() {
    return org.mockito.Mockito.mock(ShopperProfilesRequestDto.class);
  }

  private ShopperProfilesResponseDto mockShopperProfilesResponse() {
    return org.mockito.Mockito.mock(ShopperProfilesResponseDto.class);
  }
}
