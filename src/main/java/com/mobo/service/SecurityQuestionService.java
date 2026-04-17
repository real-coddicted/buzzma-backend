package com.mobo.service;

import com.mobo.dto.SecurityQuestionsRequestDto;
import com.mobo.dto.SecurityQuestionsResponseDto;
import java.util.List;
import java.util.UUID;

public interface SecurityQuestionService {

  List<SecurityQuestionsResponseDto> list(int limit, int offset);

  SecurityQuestionsResponseDto getById(UUID id);

  SecurityQuestionsResponseDto create(SecurityQuestionsRequestDto request);

  SecurityQuestionsResponseDto update(UUID id, SecurityQuestionsRequestDto request);

  void delete(UUID id);
}
