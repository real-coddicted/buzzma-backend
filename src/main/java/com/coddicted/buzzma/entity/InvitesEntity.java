package com.coddicted.buzzma.entity;

import com.coddicted.buzzma.common.AuditEntityListener;
import com.coddicted.buzzma.common.Auditable;
import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.EntityListeners;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.Id;
import jakarta.persistence.Table;
import java.time.Instant;
import java.util.UUID;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;
import org.hibernate.annotations.DynamicInsert;
import org.hibernate.annotations.DynamicUpdate;
import org.hibernate.annotations.UuidGenerator;

@Entity
@EntityListeners(AuditEntityListener.class)
@Table(name = "invites")
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
@DynamicInsert
@DynamicUpdate
public class InvitesEntity implements Auditable {
  @Id
  @GeneratedValue
  @UuidGenerator
  @Column(name = "id")
  private UUID id;

  @Column(name = "code")
  private String code;

  @Column(name = "role")
  private String role;

  @Column(name = "label")
  private String label;

  @Column(name = "parent_user_id")
  private UUID parentUserId;

  @Column(name = "parent_code")
  private String parentCode;

  @Column(name = "status")
  private String status;

  @Column(name = "max_uses")
  private Integer maxUses;

  @Column(name = "use_count")
  private Integer useCount;

  @Column(name = "expires_at")
  private Instant expiresAt;

  @Column(name = "created_by")
  private String createdBy;

  @Column(name = "used_by")
  private UUID usedBy;

  @Column(name = "used_at")
  private Instant usedAt;

  @Column(name = "uses", columnDefinition = "jsonb")
  private String uses;

  @Column(name = "revoked_by")
  private UUID revokedBy;

  @Column(name = "revoked_at")
  private Instant revokedAt;

  @Column(name = "created_at")
  private Instant createdAt;

  @Column(name = "updated_at")
  private Instant updatedAt;

  @Column(name = "updated_by")
  private String updatedBy;
}
