package com.coddicted.buzzma.orders.web;

import com.coddicted.buzzma.admin.api.AuditLogsResponseDto;
import com.coddicted.buzzma.orders.api.OrdersRequestDto;
import com.coddicted.buzzma.orders.api.OrdersResponseDto;
import com.coddicted.buzzma.orders.service.OrderDomainService;
import com.coddicted.buzzma.orders.service.OrderService;
import com.coddicted.buzzma.shared.security.CurrentUserId;
import jakarta.validation.Valid;
import jakarta.validation.constraints.Max;
import jakarta.validation.constraints.Min;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import java.util.List;
import java.util.Map;
import java.util.UUID;
import org.springframework.http.HttpStatus;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PatchMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.ResponseStatus;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/api/orders")
@Validated
public class OrdersController {

  private final OrderService service;
  private final OrderDomainService orderDomainService;

  public OrdersController(OrderService service, OrderDomainService orderDomainService) {
    this.service = service;
    this.orderDomainService = orderDomainService;
  }

  @GetMapping
  public List<OrdersResponseDto> list(
      @RequestParam(defaultValue = "50") @Min(1) @Max(500) int limit,
      @RequestParam(defaultValue = "0") @Min(0) int offset) {
    return service.list(limit, offset);
  }

  @GetMapping("/{id}")
  public OrdersResponseDto getById(@PathVariable UUID id) {
    return service.getById(id);
  }

  @PostMapping
  @ResponseStatus(HttpStatus.CREATED)
  public OrdersResponseDto create(@Valid @RequestBody OrdersRequestDto request) {
    return service.create(request);
  }

  @PatchMapping("/{id}")
  public OrdersResponseDto update(
      @PathVariable UUID id, @Valid @RequestBody OrdersRequestDto request) {
    return service.update(id, request);
  }

  @DeleteMapping("/{id}")
  @ResponseStatus(HttpStatus.NO_CONTENT)
  public void delete(@PathVariable UUID id) {
    service.delete(id);
  }

  // ── Domain endpoints ────────────────────────────────────────────────────

  @GetMapping("/user/{userId}")
  @PreAuthorize("isAuthenticated()")
  public List<OrdersResponseDto> getUserOrders(
      @PathVariable UUID userId,
      @RequestParam(defaultValue = "50") @Min(1) @Max(500) int limit,
      @RequestParam(defaultValue = "0") @Min(0) int offset) {
    return orderDomainService.getUserOrders(userId, limit, offset);
  }

  @PostMapping("/create")
  @ResponseStatus(HttpStatus.CREATED)
  @PreAuthorize("isAuthenticated()")
  public OrdersResponseDto createOrder(
      @Valid @RequestBody CreateOrderRequest request, @CurrentUserId UUID actorUserId) {
    return orderDomainService.createOrder(
        request.userId(),
        request.brandUserId(),
        request.totalPaise(),
        request.externalOrderId(),
        request.managerName(),
        request.agencyName(),
        request.buyerName(),
        request.buyerMobile(),
        request.brandName(),
        request.settlementMode(),
        request.returnWindowDays(),
        actorUserId);
  }

  @PostMapping("/claim")
  @PreAuthorize("isAuthenticated()")
  public OrdersResponseDto submitClaim(
      @Valid @RequestBody SubmitClaimRequest request, @CurrentUserId UUID actorUserId) {
    return orderDomainService.submitClaim(
        request.orderId(), request.screenshotOrder(), request.screenshotPayment(), actorUserId);
  }

  @PatchMapping("/{id}/reviewer-name")
  @PreAuthorize("isAuthenticated()")
  public OrdersResponseDto setReviewerName(
      @PathVariable UUID id,
      @RequestBody Map<String, String> body,
      @CurrentUserId UUID actorUserId) {
    return orderDomainService.setReviewerName(id, body.get("reviewerName"), actorUserId);
  }

  @GetMapping("/{id}/proof/{type}")
  @PreAuthorize("isAuthenticated()")
  public Map<String, String> getOrderProof(
      @PathVariable UUID id, @PathVariable String type, @CurrentUserId UUID actorUserId) {
    String data = orderDomainService.getOrderProof(id, type, actorUserId);
    return Map.of("data", data != null ? data : "");
  }

  @GetMapping("/{id}/proof-urls")
  @PreAuthorize("isAuthenticated()")
  public Map<String, String> getSignedProofUrls(
      @PathVariable UUID id, @CurrentUserId UUID actorUserId) {
    return orderDomainService.getSignedProofUrls(id, actorUserId);
  }

  @GetMapping("/{id}/audit")
  @PreAuthorize("isAuthenticated()")
  public List<AuditLogsResponseDto> getOrderAudit(
      @PathVariable UUID id,
      @CurrentUserId UUID actorUserId,
      @RequestParam(required = false) String role,
      @RequestParam(defaultValue = "50") @Min(1) @Max(500) int limit,
      @RequestParam(defaultValue = "0") @Min(0) int offset) {
    return orderDomainService.getOrderAudit(id, actorUserId, role, limit, offset);
  }

  // ── Request records ─────────────────────────────────────────────────────

  public record CreateOrderRequest(
      @NotNull UUID userId,
      UUID brandUserId,
      @NotNull Integer totalPaise,
      String externalOrderId,
      @NotBlank String managerName,
      String agencyName,
      @NotBlank String buyerName,
      @NotBlank String buyerMobile,
      String brandName,
      String settlementMode,
      Integer returnWindowDays) {}

  public record SubmitClaimRequest(
      @NotNull UUID orderId, String screenshotOrder, String screenshotPayment) {}
}
