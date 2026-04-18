package com.coddicted.buzzma.brands.service.impl;

import com.coddicted.buzzma.brands.persistence.BrandsEntity;
import com.coddicted.buzzma.brands.persistence.BrandsRepository;
import com.coddicted.buzzma.brands.service.BrandDomainService;
import com.coddicted.buzzma.identity.api.UserQueryPort;
import com.coddicted.buzzma.identity.api.UsersResponseDto;
import com.coddicted.buzzma.orders.api.OrderQueryPort;
import com.coddicted.buzzma.orders.api.OrdersResponseDto;
import com.coddicted.buzzma.shared.exception.ApiException;
import com.coddicted.buzzma.wallet.api.TransactionsResponseDto;
import com.coddicted.buzzma.wallet.api.WalletQueryPort;
import java.util.Arrays;
import java.util.List;
import java.util.UUID;
import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
public class BrandDomainServiceImpl implements BrandDomainService {

  private final OrderQueryPort orderQueryPort;
  private final WalletQueryPort walletQueryPort;
  private final UserQueryPort userQueryPort;
  private final BrandsRepository brandsRepository;

  public BrandDomainServiceImpl(
      OrderQueryPort orderQueryPort,
      WalletQueryPort walletQueryPort,
      UserQueryPort userQueryPort,
      BrandsRepository brandsRepository) {
    this.orderQueryPort = orderQueryPort;
    this.walletQueryPort = walletQueryPort;
    this.userQueryPort = userQueryPort;
    this.brandsRepository = brandsRepository;
  }

  @Override
  @Transactional(readOnly = true)
  public List<OrdersResponseDto> getBrandOrders(UUID brandUserId, int limit, int offset) {
    return orderQueryPort.listByBrandUserId(brandUserId, limit, offset);
  }

  @Override
  @Transactional(readOnly = true)
  public List<TransactionsResponseDto> getLedger(UUID brandUserId, int limit, int offset) {
    return walletQueryPort.listLedgerByOwnerUserId(brandUserId, limit, offset);
  }

  @Override
  @Transactional
  public void connectAgency(UUID brandUserId, String agencyCode) {
    UsersResponseDto user =
        userQueryPort
            .findById(brandUserId)
            .orElseThrow(() -> new ApiException(HttpStatus.NOT_FOUND, "USER_NOT_FOUND"));

    String brandCode = user.getBrandCode() != null ? user.getBrandCode() : "";
    BrandsEntity brand =
        brandsRepository
            .findByBrandCodeAndIsDeletedFalse(brandCode)
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
