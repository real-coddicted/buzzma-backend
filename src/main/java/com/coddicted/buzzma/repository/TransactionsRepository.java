package com.coddicted.buzzma.repository;

import com.coddicted.buzzma.entity.TransactionsEntity;
import java.util.UUID;
import org.springframework.data.jpa.repository.JpaRepository;

public interface TransactionsRepository extends JpaRepository<TransactionsEntity, UUID> {}
