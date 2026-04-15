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
@Table(name = "tickets")
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
@DynamicInsert
@DynamicUpdate
public class TicketsEntity implements Auditable {
  @Id
  @GeneratedValue
  @UuidGenerator
  @Column(name = "id")
  private UUID id;

  @Column(name = "user_id")
  private UUID userId;

  @Column(name = "user_name")
  private String userName;

  @Column(name = "role")
  private String role;

  @Column(name = "order_id")
  private String orderId;

  @Column(name = "issue_type")
  private String issueType;

  @Column(name = "description")
  private String description;

  @Column(name = "status")
  private String status;

  @Column(name = "resolved_by")
  private UUID resolvedBy;

  @Column(name = "resolved_at")
  private Instant resolvedAt;

  @Column(name = "resolution_note")
  private String resolutionNote;

  @Column(name = "created_by")
  private String createdBy;

  @Column(name = "updated_by")
  private String updatedBy;

  @Column(name = "created_at")
  private Instant createdAt;

  @Column(name = "updated_at")
  private Instant updatedAt;

  @Column(name = "is_deleted")
  private Boolean isDeleted;

  @Column(name = "target_role")
  private String targetRole;

  @Column(name = "priority")
  private String priority;
}
