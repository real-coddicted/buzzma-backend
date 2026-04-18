package com.coddicted.buzzma.brands.web;

import com.coddicted.buzzma.brands.service.BrandDomainService;
import com.coddicted.buzzma.orders.api.OrdersResponseDto;
import com.coddicted.buzzma.shared.security.CurrentUserId;
import com.coddicted.buzzma.wallet.api.TransactionsResponseDto;
import jakarta.validation.constraints.Max;
import jakarta.validation.constraints.Min;
import jakarta.validation.constraints.NotBlank;
import java.util.List;
import java.util.UUID;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/api/brand")
@Validated
@PreAuthorize("hasRole('brand')")
public class BrandController {

  private final BrandDomainService brandDomainService;

  public BrandController(BrandDomainService brandDomainService) {
    this.brandDomainService = brandDomainService;
  }

  @GetMapping("/orders")
  public List<OrdersResponseDto> getOrders(
      @CurrentUserId UUID brandUserId,
      @RequestParam(defaultValue = "50") @Min(1) @Max(500) int limit,
      @RequestParam(defaultValue = "0") @Min(0) int offset) {
    return brandDomainService.getBrandOrders(brandUserId, limit, offset);
  }

  @GetMapping("/ledger")
  public List<TransactionsResponseDto> getLedger(
      @CurrentUserId UUID brandUserId,
      @RequestParam(defaultValue = "50") @Min(1) @Max(500) int limit,
      @RequestParam(defaultValue = "0") @Min(0) int offset) {
    return brandDomainService.getLedger(brandUserId, limit, offset);
  }

  @PostMapping("/connect-agency")
  public void connectAgency(
      @CurrentUserId UUID brandUserId, @RequestParam @NotBlank String agencyCode) {
    brandDomainService.connectAgency(brandUserId, agencyCode);
  }
}
