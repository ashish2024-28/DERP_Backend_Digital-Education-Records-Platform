package com.demoproject.Entity.FeesAccounts;

import com.demoproject.Entity.FeesAdmin;
import jakarta.persistence.*;
import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

@Entity @Table(name="fee_payment_method")
@Getter @Setter @NoArgsConstructor @AllArgsConstructor

//payment method types.
public class FeesPaymentMethod {
 @Id
 @GeneratedValue(strategy=GenerationType.IDENTITY)
 private Long id;

 @Enumerated(EnumType.STRING)
 @Column(nullable=false)
 private FeesPaymentMethodType paymentType;

 @Column(nullable=false)
 private String displayName;
 private String bankName;
 private String accountName;
 private String accountNumber;
 private String ifsc;
 private String branch;

 private String upiId;
 private String qrImagePath;

 private String instructions;

 @Column(nullable=false)
 private boolean active = true;

 @ManyToOne(fetch=FetchType.LAZY,optional=false)
 @JoinColumn(name="fees_admin_id")
 private FeesAdmin feesAdmin;

}
