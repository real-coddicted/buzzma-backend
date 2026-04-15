package com.coddicted.buzzma.common;

import java.util.UUID;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.http.HttpStatus;
import org.springframework.web.server.ResponseStatusException;

public abstract class BaseCrudService<E, Req, Res> {
  protected E mustFind(
      final JpaRepository<E, UUID> repository, final UUID id, final String resourceName) {
    return repository
        .findById(id)
        .orElseThrow(
            () ->
                new ResponseStatusException(
                    HttpStatus.NOT_FOUND, "%s not found: %s".formatted(resourceName, id)));
  }
}
