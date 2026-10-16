package com.demoproject.Entity.FeesAccounts;

import com.demoproject.Entity.FeesAdmin;
import com.demoproject.Entity.Student;
import jakarta.persistence.*;
import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

import java.time.LocalDateTime;

@Entity
@Table(name="student_fee_payment")
@Getter @Setter @NoArgsConstructor @AllArgsConstructor

//  submitted payments by students with proof, UPI/reference, status
public class StudentFeesPayment {

 @Id @GeneratedValue(strategy=GenerationType.IDENTITY)
 private Long id;

 @ManyToOne(fetch=FetchType.LAZY,optional=false)
 @JoinColumn(name="student_id")
 private Student student;

 @Column(nullable=false)
 private String academicSession;

 @Column(nullable=false)
 private Double amount;

 @Column(unique = true)
 private String upiTransactionId;


 @Enumerated(EnumType.STRING)
 @Column(nullable=false)
 private FeesPaymentMethodType paymentType;

 @Enumerated(EnumType.STRING)
 @Column(nullable=false)
 private FeesPaymentStatus status= FeesPaymentStatus.PENDING;

 private String rejectionReason;


 @ManyToOne(fetch=FetchType.LAZY)
 @JoinColumn(name="verified_by_fees_admin_id", nullable=true)
 private FeesAdmin verifyByFeesAdmin;

 private LocalDateTime verifiedAt;

}
