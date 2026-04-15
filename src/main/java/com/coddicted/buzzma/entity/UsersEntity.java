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
@Table(name = "users")
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
@DynamicInsert
@DynamicUpdate
public class UsersEntity implements Auditable {
  @Id
  @GeneratedValue
  @UuidGenerator
  @Column(name = "id")
  private UUID id;

  @Column(name = "name")
  private String name;

  @Column(name = "username")
  private String username;

  @Column(name = "mobile")
  private String mobile;

  @Column(name = "email")
  private String email;

  @Column(name = "password_hash")
  private String passwordHash;

  @Column(name = "role")
  private String role;

  @Column(name = "roles")
  private String[] roles;

  @Column(name = "status")
  private String status;

  @Column(name = "mediator_code")
  private String mediatorCode;

  @Column(name = "parent_code")
  private String parentCode;

  @Column(name = "generated_codes")
  private String[] generatedCodes;

  @Column(name = "is_verified_by_mediator")
  private Boolean isVerifiedByMediator;

  @Column(name = "brand_code")
  private String brandCode;

  @Column(name = "connected_agencies")
  private String[] connectedAgencies;

  @Column(name = "kyc_status")
  private String kycStatus;

  @Column(name = "kyc_pan_card")
  private String kycPanCard;

  @Column(name = "kyc_aadhaar")
  private String kycAadhaar;

  @Column(name = "kyc_gst")
  private String kycGst;

  @Column(name = "upi_id")
  private String upiId;

  @Column(name = "qr_code")
  private String qrCode;

  @Column(name = "bank_account_number")
  private String bankAccountNumber;

  @Column(name = "bank_ifsc")
  private String bankIfsc;

  @Column(name = "bank_name")
  private String bankName;

  @Column(name = "bank_holder_name")
  private String bankHolderName;

  @Column(name = "wallet_balance_paise")
  private Integer walletBalancePaise;

  @Column(name = "wallet_pending_paise")
  private Integer walletPendingPaise;

  @Column(name = "avatar")
  private String avatar;

  @Column(name = "failed_login_attempts")
  private Integer failedLoginAttempts;

  @Column(name = "lockout_until")
  private Instant lockoutUntil;

  @Column(name = "google_refresh_token")
  private String googleRefreshToken;

  @Column(name = "google_email")
  private String googleEmail;

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
}
