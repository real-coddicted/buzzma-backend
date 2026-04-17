package com.mobo.controller;

import com.mobo.dto.TicketCommentsResponseDto;
import com.mobo.dto.TicketsRequestDto;
import com.mobo.dto.TicketsResponseDto;
import com.mobo.security.CurrentUserId;
import com.mobo.service.TicketDomainService;
import com.mobo.service.TicketService;
import jakarta.validation.Valid;
import jakarta.validation.constraints.Max;
import jakarta.validation.constraints.Min;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Size;
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
@RequestMapping("/api/tickets")
@Validated
public class TicketsController {

  private final TicketService service;
  private final TicketDomainService ticketDomainService;

  public TicketsController(TicketService service, TicketDomainService ticketDomainService) {
    this.service = service;
    this.ticketDomainService = ticketDomainService;
  }

  @GetMapping
  public List<TicketsResponseDto> list(
      @RequestParam(defaultValue = "50") @Min(1) @Max(500) int limit,
      @RequestParam(defaultValue = "0") @Min(0) int offset) {
    return service.list(limit, offset);
  }

  @GetMapping("/{id}")
  public TicketsResponseDto getById(@PathVariable UUID id) {
    return service.getById(id);
  }

  @PostMapping
  @ResponseStatus(HttpStatus.CREATED)
  public TicketsResponseDto create(@Valid @RequestBody TicketsRequestDto request) {
    return service.create(request);
  }

  @PatchMapping("/{id}")
  public TicketsResponseDto update(
      @PathVariable UUID id, @Valid @RequestBody TicketsRequestDto request) {
    return service.update(id, request);
  }

  @DeleteMapping("/{id}")
  @ResponseStatus(HttpStatus.NO_CONTENT)
  public void delete(@PathVariable UUID id) {
    service.delete(id);
  }

  @PostMapping("/{id}/resolve")
  @PreAuthorize("isAuthenticated()")
  public TicketsResponseDto resolveTicket(
      @PathVariable UUID id, @RequestBody Map<String, String> body, @CurrentUserId UUID actorId) {
    return ticketDomainService.resolveTicket(id, body.get("note"), actorId);
  }

  @PostMapping("/{id}/reject")
  @PreAuthorize("isAuthenticated()")
  public TicketsResponseDto rejectTicket(
      @PathVariable UUID id, @RequestBody Map<String, String> body, @CurrentUserId UUID actorId) {
    return ticketDomainService.rejectTicket(id, body.get("note"), actorId);
  }

  @PostMapping("/{id}/comments")
  @ResponseStatus(HttpStatus.CREATED)
  @PreAuthorize("isAuthenticated()")
  public TicketCommentsResponseDto addComment(
      @PathVariable UUID id,
      @Valid @RequestBody AddCommentRequest request,
      @CurrentUserId UUID actorId) {
    return ticketDomainService.addComment(
        id, request.message(), actorId, request.userName(), request.role());
  }

  public record AddCommentRequest(
      @NotBlank @Size(max = 2000) String message,
      @NotBlank String userName,
      @NotBlank String role) {}
}
