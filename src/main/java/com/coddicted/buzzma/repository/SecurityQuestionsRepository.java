package com.coddicted.buzzma.repository;

import com.coddicted.buzzma.entity.SecurityQuestionsEntity;
import java.util.UUID;
import org.springframework.data.jpa.repository.JpaRepository;

public interface SecurityQuestionsRepository extends JpaRepository<SecurityQuestionsEntity, UUID> {}
