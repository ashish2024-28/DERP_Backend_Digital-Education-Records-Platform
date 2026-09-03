package com.demoproject.Entity.FeesAcounts;

import jakarta.persistence.*;
import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;
@Entity @Table(name="erp_fee_payment_method")
@Getter @Setter @NoArgsConstructor @AllArgsConstructor

//payment method types.
public class FeePaymentMethod {
 @Id @GeneratedValue(strategy=GenerationType.IDENTITY) private Long id;
 @Column(nullable=false) private String domain;
 @Enumerated(EnumType.STRING) @Column(nullable=false) private FeePaymentMethodType type;
 @Column(nullable=false) private String displayName;
 private String bankName;
 private String accountName;
 private String accountNumber;
 private String ifsc;
 private String branch;
 private String upiId;
 private String qrImagePath;
 private String instructions;
 @Column(nullable=false) private boolean active=true;
}
