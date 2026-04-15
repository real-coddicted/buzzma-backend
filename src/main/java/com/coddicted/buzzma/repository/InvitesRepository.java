package com.coddicted.buzzma.repository;

import com.coddicted.buzzma.entity.InvitesEntity;
import java.util.UUID;
import org.springframework.data.jpa.repository.JpaRepository;

public interface InvitesRepository extends JpaRepository<InvitesEntity, UUID> {}
