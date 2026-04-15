package com.coddicted.buzzma.controller;

import com.coddicted.buzzma.dto.OrderItemsRequestDto;
import com.coddicted.buzzma.dto.OrderItemsResponseDto;
import com.coddicted.buzzma.service.OrderItemsService;
import jakarta.validation.Valid;
import jakarta.validation.constraints.Max;
import jakarta.validation.constraints.Min;
import java.util.List;
import java.util.UUID;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PatchMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

@RestController
@Validated
@RequestMapping("/api/order_items")
public class OrderItemsController {
  private final OrderItemsService service;

  public OrderItemsController(final OrderItemsService service) {
    this.service = service;
  }

  @GetMapping
  public List<OrderItemsResponseDto> list(
      @RequestParam(defaultValue = "50") @Min(1) @Max(500) final int limit,
      @RequestParam(defaultValue = "0") @Min(0) final int offset) {
    return service.list(limit, offset);
  }

  @GetMapping("/{id}")
  public OrderItemsResponseDto getById(@PathVariable final UUID id) {
    return service.getById(id);
  }

  @PostMapping
  public OrderItemsResponseDto create(@RequestBody @Valid final OrderItemsRequestDto request) {
    return service.create(request);
  }

  @PatchMapping("/{id}")
  public OrderItemsResponseDto update(
      @PathVariable final UUID id, @RequestBody @Valid final OrderItemsRequestDto request) {
    return service.update(id, request);
  }

  @DeleteMapping("/{id}")
  public void delete(@PathVariable final UUID id) {
    service.delete(id);
  }
}
