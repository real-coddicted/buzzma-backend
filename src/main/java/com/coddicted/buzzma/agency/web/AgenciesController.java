package com.coddicted.buzzma.agency.web;

import com.coddicted.buzzma.agency.api.AgenciesRequestDto;
import com.coddicted.buzzma.agency.api.AgenciesResponseDto;
import com.coddicted.buzzma.agency.service.AgencyService;
import jakarta.validation.Valid;
import jakarta.validation.constraints.Max;
import jakarta.validation.constraints.Min;
import java.util.List;
import java.util.UUID;
import org.springframework.http.HttpStatus;
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
@RequestMapping("/api/agencies")
@Validated
public class AgenciesController {

  private final AgencyService service;

  public AgenciesController(AgencyService service) {
    this.service = service;
  }

  @GetMapping
  public List<AgenciesResponseDto> list(
      @RequestParam(defaultValue = "50") @Min(1) @Max(500) int limit,
      @RequestParam(defaultValue = "0") @Min(0) int offset) {
    return service.list(limit, offset);
  }

  @GetMapping("/{id}")
  public AgenciesResponseDto getById(@PathVariable UUID id) {
    return service.getById(id);
  }

  @PostMapping
  @ResponseStatus(HttpStatus.CREATED)
  public AgenciesResponseDto create(@Valid @RequestBody AgenciesRequestDto request) {
    return service.create(request);
  }

  @PatchMapping("/{id}")
  public AgenciesResponseDto update(
      @PathVariable UUID id, @Valid @RequestBody AgenciesRequestDto request) {
    return service.update(id, request);
  }

  @DeleteMapping("/{id}")
  @ResponseStatus(HttpStatus.NO_CONTENT)
  public void delete(@PathVariable UUID id) {
    service.delete(id);
  }
}
