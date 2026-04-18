package com.mobo.admin.persistence;

import com.mobo.shared.enums.SuspensionAction;
import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.EnumType;
import jakarta.persistence.Enumerated;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.Id;
import jakarta.persistence.Table;
import java.time.Instant;
import java.util.UUID;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;
import org.hibernate.annotations.UuidGenerator;

@Entity
@Table(name = "suspensions")
@Getter
@Setter
@NoArgsConstructor
public class SuspensionsEntity {

  @Id
  @GeneratedValue
  @UuidGenerator
  @Column(name = "id", updatable = false, nullable = false)
  private UUID id;

  @Column(name = "target_user_id", nullable = false)
  private UUID targetUserId;

  @Enumerated(EnumType.STRING)
  @Column(name = "action", nullable = false)
  private SuspensionAction action;

  @Column(name = "reason")
  private String reason;

  @Column(name = "admin_user_id", nullable = false)
  private UUID adminUserId;

  @Column(name = "created_at", nullable = false, updatable = false)
  private Instant createdAt = Instant.now();
}
