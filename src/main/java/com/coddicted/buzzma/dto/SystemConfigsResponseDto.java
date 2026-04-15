package com.coddicted.buzzma.dto;

import java.time.Instant;
import java.util.UUID;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class SystemConfigsResponseDto {
  private UUID id;
  private String key;
  private String adminContactEmail;
  private Instant createdAt;
  private Instant updatedAt;
  private String createdBy;
  private String updatedBy;
}
