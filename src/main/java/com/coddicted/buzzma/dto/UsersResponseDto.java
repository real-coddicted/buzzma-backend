package com.coddicted.buzzma.dto;

import java.time.Instant;
import java.util.UUID;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class UsersResponseDto {
  private UUID id;
  private String name;
  private String username;
  private String mobile;
  private String email;
  private String passwordHash;
  private String role;
  private String[] roles;
  private String status;
  private String mediatorCode;
  private String parentCode;
  private String[] generatedCodes;
  private Boolean isVerifiedByMediator;
  private String brandCode;
  private String[] connectedAgencies;
  private String kycStatus;
  private String kycPanCard;
  private String kycAadhaar;
  private String kycGst;
  private String upiId;
  private String qrCode;
  private String bankAccountNumber;
  private String bankIfsc;
  private String bankName;
  private String bankHolderName;
  private Integer walletBalancePaise;
  private Integer walletPendingPaise;
  private String avatar;
  private Integer failedLoginAttempts;
  private Instant lockoutUntil;
  private String googleRefreshToken;
  private String googleEmail;
  private String createdBy;
  private String updatedBy;
  private Instant createdAt;
  private Instant updatedAt;
  private Boolean isDeleted;
}
