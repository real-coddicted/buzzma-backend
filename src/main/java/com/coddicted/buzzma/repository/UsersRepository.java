package com.coddicted.buzzma.repository;

import com.coddicted.buzzma.entity.UsersEntity;
import java.util.UUID;
import org.springframework.data.jpa.repository.JpaRepository;

public interface UsersRepository extends JpaRepository<UsersEntity, UUID> {}
