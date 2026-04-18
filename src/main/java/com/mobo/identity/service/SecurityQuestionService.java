package com.mobo.identity.service;

import com.mobo.identity.api.SecurityQuestionsRequestDto;
import com.mobo.identity.api.SecurityQuestionsResponseDto;
import java.util.List;
import java.util.UUID;

public interface SecurityQuestionService {

  List<SecurityQuestionsResponseDto> list(int limit, int offset);

  SecurityQuestionsResponseDto getById(UUID id);

  SecurityQuestionsResponseDto create(SecurityQuestionsRequestDto request);

  SecurityQuestionsResponseDto update(UUID id, SecurityQuestionsRequestDto request);

  void delete(UUID id);
}
