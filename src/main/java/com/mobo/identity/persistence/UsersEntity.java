package com.mobo.identity.persistence;

import com.mobo.shared.common.AuditEntityListener;
import com.mobo.shared.common.Auditable;
import com.mobo.shared.enums.KycStatus;
import com.mobo.shared.enums.UserRole;
import com.mobo.shared.enums.UserStatus;
import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.EntityListeners;
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
@Table(name = "users")
@EntityListeners(AuditEntityListener.class)
@Getter
@Setter
@NoArgsConstructor
public class UsersEntity implements Auditable {

  @Id
  @GeneratedValue
  @UuidGenerator
  @Column(name = "id", updatable = false, nullable = false)
  private UUID id;

  @Column(name = "name", length = 120, nullable = false)
  private String name;

  @Column(name = "username", length = 64, unique = true)
  private String username;

  @Column(name = "mobile", length = 10, nullable = false)
  private String mobile;

  @Column(name = "email", length = 320)
  private String email;

  @Column(name = "password_hash", nullable = false)
  private String passwordHash;

  @Enumerated(EnumType.STRING)
  @Column(name = "role")
  private UserRole role = UserRole.shopper;

  @Column(name = "roles", columnDefinition = "user_role[]")
  private String[] roles = new String[] {"shopper"};

  @Enumerated(EnumType.STRING)
  @Column(name = "status")
  private UserStatus status = UserStatus.active;

  @Column(name = "mediator_code", length = 64)
  private String mediatorCode;

  @Column(name = "parent_code", length = 64)
  private String parentCode;

  @Column(name = "generated_codes", columnDefinition = "text[]")
  private String[] generatedCodes = new String[0];

  @Column(name = "is_verified_by_mediator")
  private Boolean isVerifiedByMediator = false;

  @Column(name = "brand_code", length = 64)
  private String brandCode;

  @Column(name = "connected_agencies", columnDefinition = "text[]")
  private String[] connectedAgencies = new String[0];

  @Enumerated(EnumType.STRING)
  @Column(name = "kyc_status")
  private KycStatus kycStatus = KycStatus.none;

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
  private Integer walletBalancePaise = 0;

  @Column(name = "wallet_pending_paise")
  private Integer walletPendingPaise = 0;

  @Column(name = "avatar")
  private String avatar;

  @Column(name = "failed_login_attempts")
  private Integer failedLoginAttempts = 0;

  @Column(name = "lockout_until")
  private Instant lockoutUntil;

  @Column(name = "google_refresh_token")
  private String googleRefreshToken;

  @Column(name = "google_email", length = 320)
  private String googleEmail;

  @Column(name = "created_by")
  private UUID createdBy;

  @Column(name = "updated_by")
  private UUID updatedBy;

  @Column(name = "is_deleted", nullable = false)
  private Boolean isDeleted = false;

  @Column(name = "created_at", nullable = false, updatable = false)
  private Instant createdAt;

  @Column(name = "updated_at", nullable = false)
  private Instant updatedAt;
}
