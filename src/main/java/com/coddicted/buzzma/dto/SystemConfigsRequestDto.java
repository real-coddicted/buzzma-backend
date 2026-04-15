package com.coddicted.buzzma.dto;

import jakarta.validation.constraints.NotNull;
import java.time.Instant;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class SystemConfigsRequestDto {
  private String key;
  private String adminContactEmail;
  private Instant createdAt;
  @NotNull private Instant updatedAt;
  private String createdBy;
  private String updatedBy;
}
