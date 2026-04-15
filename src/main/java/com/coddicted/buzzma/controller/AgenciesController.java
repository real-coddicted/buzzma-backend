package com.coddicted.buzzma.controller;

import com.coddicted.buzzma.dto.AgenciesRequestDto;
import com.coddicted.buzzma.dto.AgenciesResponseDto;
import com.coddicted.buzzma.service.AgenciesService;
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
@RequestMapping("/api/agencies")
public class AgenciesController {
  private final AgenciesService service;

  public AgenciesController(final AgenciesService service) {
    this.service = service;
  }

  @GetMapping
  public List<AgenciesResponseDto> list(
      @RequestParam(defaultValue = "50") @Min(1) @Max(500) final int limit,
      @RequestParam(defaultValue = "0") @Min(0) final int offset) {
    return service.list(limit, offset);
  }

  @GetMapping("/{id}")
  public AgenciesResponseDto getById(@PathVariable final UUID id) {
    return service.getById(id);
  }

  @PostMapping
  public AgenciesResponseDto create(@RequestBody @Valid final AgenciesRequestDto request) {
    return service.create(request);
  }

  @PatchMapping("/{id}")
  public AgenciesResponseDto update(
      @PathVariable final UUID id, @RequestBody @Valid final AgenciesRequestDto request) {
    return service.update(id, request);
  }

  @DeleteMapping("/{id}")
  public void delete(@PathVariable final UUID id) {
    service.delete(id);
  }
}
