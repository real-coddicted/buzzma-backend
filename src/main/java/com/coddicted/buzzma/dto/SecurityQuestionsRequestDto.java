package com.coddicted.buzzma.dto;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
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
public class SecurityQuestionsRequestDto {
  @NotNull private UUID userId;
  @NotNull private Integer questionId;
  @NotBlank private String answerHash;
  private Instant createdAt;
  @NotNull private Instant updatedAt;
  private String createdBy;
  private String updatedBy;
}
