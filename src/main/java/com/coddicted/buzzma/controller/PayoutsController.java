package com.coddicted.buzzma.controller;

import com.coddicted.buzzma.dto.PayoutsRequestDto;
import com.coddicted.buzzma.dto.PayoutsResponseDto;
import com.coddicted.buzzma.service.PayoutsService;
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
@RequestMapping("/api/payouts")
public class PayoutsController {
  private final PayoutsService service;

  public PayoutsController(final PayoutsService service) {
    this.service = service;
  }

  @GetMapping
  public List<PayoutsResponseDto> list(
      @RequestParam(defaultValue = "50") @Min(1) @Max(500) final int limit,
      @RequestParam(defaultValue = "0") @Min(0) final int offset) {
    return service.list(limit, offset);
  }

  @GetMapping("/{id}")
  public PayoutsResponseDto getById(@PathVariable final UUID id) {
    return service.getById(id);
  }

  @PostMapping
  public PayoutsResponseDto create(@RequestBody @Valid final PayoutsRequestDto request) {
    return service.create(request);
  }

  @PatchMapping("/{id}")
  public PayoutsResponseDto update(
      @PathVariable final UUID id, @RequestBody @Valid final PayoutsRequestDto request) {
    return service.update(id, request);
  }

  @DeleteMapping("/{id}")
  public void delete(@PathVariable final UUID id) {
    service.delete(id);
  }
}
