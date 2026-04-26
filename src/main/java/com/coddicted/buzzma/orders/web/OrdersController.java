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
import java.util.ArrayList;
import java.util.LinkedHashMap;
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
  public List<Map<String, Object>> getUserOrders(
      @PathVariable UUID userId,
      @RequestParam(defaultValue = "50") @Min(1) @Max(500) int limit,
      @RequestParam(defaultValue = "0") @Min(0) int offset) {
    return orderDomainService.getUserOrders(userId, limit, offset).stream()
        .map(OrdersController::toUiOrder)
        .toList();
  }

  private static Map<String, Object> toUiOrder(OrdersResponseDto o) {
    // screenshots flags (no base64 blobs)
    Map<String, Object> screenshots = new LinkedHashMap<>();
    screenshots.put("order", o.getScreenshotOrder() != null ? "exists" : null);
    screenshots.put("payment", o.getScreenshotPayment() != null ? "exists" : null);
    screenshots.put("review", o.getScreenshotReview() != null ? "exists" : null);
    screenshots.put("rating", o.getScreenshotRating() != null ? "exists" : null);
    screenshots.put("returnWindow", o.getScreenshotReturnWindow() != null ? "exists" : null);

    // verification object from flat jsonb field (parse minimally)
    boolean orderVerified = false;
    boolean reviewVerified = false;
    boolean ratingVerified = false;
    boolean returnWindowVerified = false;
    if (o.getVerification() != null && !o.getVerification().isBlank()) {
      String v = o.getVerification();
      orderVerified = v.contains("\"order\"") && v.contains("verifiedAt");
      reviewVerified = v.contains("\"review\"") && v.contains("verifiedAt");
      ratingVerified = v.contains("\"rating\"") && v.contains("verifiedAt");
      returnWindowVerified = v.contains("\"returnWindow\"") && v.contains("verifiedAt");
    }
    Map<String, Object> verification = new LinkedHashMap<>();
    verification.put("orderVerified", orderVerified);
    verification.put("reviewVerified", reviewVerified);
    verification.put("ratingVerified", ratingVerified);
    verification.put("returnWindowVerified", returnWindowVerified);

    // requirements: all orders require returnWindow; review/rating depend on dealType
    // Without item dealType we conservatively mark returnWindow only
    List<String> requiredSteps = new ArrayList<>();
    requiredSteps.add("returnWindow");
    List<String> missingProofs = new ArrayList<>();
    if (o.getScreenshotReturnWindow() == null) {
      missingProofs.add("returnWindow");
    }
    List<String> missingVerifications = new ArrayList<>();
    if (!returnWindowVerified) {
      missingVerifications.add("returnWindow");
    }

    Map<String, Object> requirements = new LinkedHashMap<>();
    requirements.put("required", requiredSteps);
    requirements.put("missingProofs", missingProofs);
    requirements.put("missingVerifications", missingVerifications);

    // rejection
    Map<String, Object> rejection = null;
    if (o.getRejectionType() != null) {
      rejection = new LinkedHashMap<>();
      rejection.put("type", o.getRejectionType());
      rejection.put("reason", o.getRejectionReason());
    }

    // synthetic items array from flat order fields
    Map<String, Object> item = new LinkedHashMap<>();
    item.put("title", o.getBrandName() != null ? o.getBrandName() : "Order");
    item.put("image", null);
    item.put("dealType", "Rating");
    item.put("quantity", 1);
    item.put("platform", null);
    item.put("brandName", o.getBrandName());
    item.put("priceAtPurchase", o.getTotalPaise() != null ? o.getTotalPaise() / 100.0 : 0);
    item.put("commission", 0);

    Map<String, Object> result = new LinkedHashMap<>();
    result.put("id", o.getId());
    result.put("userId", o.getUserId());
    result.put("items", List.of(item));
    result.put("total", o.getTotalPaise() != null ? o.getTotalPaise() / 100.0 : 0);
    result.put("status", o.getStatus());
    result.put("workflowStatus", o.getWorkflowStatus());
    result.put("frozen", Boolean.TRUE.equals(o.getFrozen()));
    result.put("frozenAt", o.getFrozenAt());
    result.put("frozenReason", o.getFrozenReason());
    result.put("paymentStatus", o.getPaymentStatus());
    result.put("affiliateStatus", o.getAffiliateStatus());
    result.put("externalOrderId", o.getExternalOrderId());
    result.put("settlementMode", o.getSettlementMode());
    result.put("screenshots", screenshots);
    result.put("reviewLink", o.getReviewLink());
    result.put("verification", verification);
    result.put("requirements", requirements);
    result.put("missingProofRequests", List.of());
    result.put("rejection", rejection);
    result.put("managerName", o.getManagerName());
    result.put("agencyName", o.getAgencyName());
    result.put("buyerName", o.getBuyerName());
    result.put("reviewerName", o.getReviewerName());
    result.put("brandName", o.getBrandName());
    result.put("createdAt", o.getCreatedAt());
    result.put("expectedSettlementDate", o.getExpectedSettlementDate());
    return result;
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
