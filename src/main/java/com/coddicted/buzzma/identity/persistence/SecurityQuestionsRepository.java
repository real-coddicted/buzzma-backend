package com.coddicted.buzzma.identity.persistence;

import java.util.List;
import java.util.Optional;
import java.util.UUID;
import org.springframework.data.jpa.repository.JpaRepository;

public interface SecurityQuestionsRepository extends JpaRepository<SecurityQuestionsEntity, UUID> {

  List<SecurityQuestionsEntity> findAllByUserId(UUID userId);

  Optional<SecurityQuestionsEntity> findByUserIdAndQuestionId(UUID userId, Integer questionId);
}
