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
public class SecurityQuestionsResponseDto {
  private UUID id;
  private UUID userId;
  private Integer questionId;
  private String answerHash;
  private Instant createdAt;
  private Instant updatedAt;
  private String createdBy;
  private String updatedBy;
}
