package com.mobo.service.impl;

import com.mobo.common.OffsetBasedPageRequest;
import com.mobo.dto.OrdersResponseDto;
import com.mobo.dto.TransactionsResponseDto;
import com.mobo.entity.BrandsEntity;
import com.mobo.entity.UsersEntity;
import com.mobo.entity.WalletsEntity;
import com.mobo.exception.ApiException;
import com.mobo.mapper.OrdersMapper;
import com.mobo.mapper.TransactionsMapper;
import com.mobo.repository.BrandsRepository;
import com.mobo.repository.OrdersRepository;
import com.mobo.repository.TransactionsRepository;
import com.mobo.repository.UsersRepository;
import com.mobo.repository.WalletsRepository;
import com.mobo.service.BrandDomainService;
import java.util.Arrays;
import java.util.List;
import java.util.UUID;
import java.util.stream.Collectors;
import org.springframework.data.domain.Sort;
import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
public class BrandDomainServiceImpl implements BrandDomainService {

  private final OrdersRepository ordersRepository;
  private final TransactionsRepository transactionsRepository;
  private final WalletsRepository walletsRepository;
  private final UsersRepository usersRepository;
  private final BrandsRepository brandsRepository;
  private final OrdersMapper ordersMapper;
  private final TransactionsMapper transactionsMapper;

  public BrandDomainServiceImpl(
      OrdersRepository ordersRepository,
      TransactionsRepository transactionsRepository,
      WalletsRepository walletsRepository,
      UsersRepository usersRepository,
      BrandsRepository brandsRepository,
      OrdersMapper ordersMapper,
      TransactionsMapper transactionsMapper) {
    this.ordersRepository = ordersRepository;
    this.transactionsRepository = transactionsRepository;
    this.walletsRepository = walletsRepository;
    this.usersRepository = usersRepository;
    this.brandsRepository = brandsRepository;
    this.ordersMapper = ordersMapper;
    this.transactionsMapper = transactionsMapper;
  }

  @Override
  @Transactional(readOnly = true)
  public List<OrdersResponseDto> getBrandOrders(UUID brandUserId, int limit, int offset) {
    var pageable =
        new OffsetBasedPageRequest(limit, offset, Sort.by(Sort.Direction.DESC, "createdAt"));
    return ordersRepository.findAllByBrandUserIdAndIsDeletedFalse(brandUserId, pageable).stream()
        .map(ordersMapper::toResponse)
        .collect(Collectors.toList());
  }

  @Override
  @Transactional(readOnly = true)
  public List<TransactionsResponseDto> getLedger(UUID brandUserId, int limit, int offset) {
    WalletsEntity wallet =
        walletsRepository
            .findByOwnerUserId(brandUserId)
            .orElseThrow(() -> new ApiException(HttpStatus.NOT_FOUND, "WALLET_NOT_FOUND"));

    var pageable =
        new OffsetBasedPageRequest(limit, offset, Sort.by(Sort.Direction.DESC, "createdAt"));
    return transactionsRepository
        .findAllByWalletIdAndIsDeletedFalse(wallet.getId(), pageable)
        .stream()
        .map(transactionsMapper::toResponse)
        .collect(Collectors.toList());
  }

  @Override
  @Transactional
  public void connectAgency(UUID brandUserId, String agencyCode) {
    UsersEntity user =
        usersRepository
            .findById(brandUserId)
            .filter(u -> !Boolean.TRUE.equals(u.getIsDeleted()))
            .orElseThrow(() -> new ApiException(HttpStatus.NOT_FOUND, "USER_NOT_FOUND"));

    BrandsEntity brand =
        brandsRepository
            .findByBrandCodeAndIsDeletedFalse(
                user.getBrandCode() != null ? user.getBrandCode() : "")
            .orElseThrow(() -> new ApiException(HttpStatus.NOT_FOUND, "BRAND_NOT_FOUND"));

    String[] existing =
        brand.getConnectedAgencyCodes() != null ? brand.getConnectedAgencyCodes() : new String[0];
    boolean alreadyConnected = Arrays.stream(existing).anyMatch(c -> c.equals(agencyCode));
    if (alreadyConnected) {
      return; // idempotent
    }

    String[] updated = Arrays.copyOf(existing, existing.length + 1);
    updated[existing.length] = agencyCode;
    brand.setConnectedAgencyCodes(updated);
    brandsRepository.save(brand);
  }
}
