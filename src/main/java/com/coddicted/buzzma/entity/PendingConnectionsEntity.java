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
@Table(name = "pending_connections")
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
@DynamicInsert
@DynamicUpdate
public class PendingConnectionsEntity implements Auditable {
  @Id
  @GeneratedValue
  @UuidGenerator
  @Column(name = "id")
  private UUID id;

  @Column(name = "user_id")
  private UUID userId;

  @Column(name = "agency_id")
  private String agencyId;

  @Column(name = "agency_name")
  private String agencyName;

  @Column(name = "agency_code")
  private String agencyCode;

  @Column(name = "timestamp")
  private Instant timestamp;

  @Column(name = "is_deleted")
  private Boolean isDeleted;

  @Column(name = "created_by")
  private String createdBy;

  @Column(name = "updated_by")
  private String updatedBy;

  @Column(name = "created_at")
  private Instant createdAt;

  @Column(name = "updated_at")
  private Instant updatedAt;
}
