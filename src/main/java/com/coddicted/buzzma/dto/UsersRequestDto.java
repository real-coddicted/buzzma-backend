package com.coddicted.buzzma.dto;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Size;
import java.time.Instant;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class UsersRequestDto {
  @NotBlank
  @Size(max = 120)
  private String name;

  @Size(max = 64)
  private String username;

  @NotBlank
  @Size(max = 10)
  private String mobile;

  @Size(max = 320)
  private String email;

  @NotBlank private String passwordHash;
  private String role;
  private String[] roles;
  private String status;

  @Size(max = 64)
  private String mediatorCode;

  @Size(max = 64)
  private String parentCode;

  private String[] generatedCodes;
  private Boolean isVerifiedByMediator;

  @Size(max = 64)
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

  @Size(max = 320)
  private String googleEmail;

  private String createdBy;
  private String updatedBy;
  private Instant createdAt;
  @NotNull private Instant updatedAt;
  private Boolean isDeleted;
}
