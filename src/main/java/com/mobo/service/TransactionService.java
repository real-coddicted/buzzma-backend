package com.mobo.service;

import com.mobo.dto.TransactionsRequestDto;
import com.mobo.dto.TransactionsResponseDto;
import java.util.List;
import java.util.UUID;

public interface TransactionService {

  List<TransactionsResponseDto> list(int limit, int offset);

  TransactionsResponseDto getById(UUID id);

  TransactionsResponseDto create(TransactionsRequestDto request);

  TransactionsResponseDto update(UUID id, TransactionsRequestDto request);

  void delete(UUID id);
}
