//package com.demoproject.Entity.FeesAcounts;
//
//import com.demoproject.Entity.FeePaymentStatus;
//import com.demoproject.Entity.Student;
//import jakarta.persistence.*;
//import lombok.AllArgsConstructor;
//import lombok.Getter;
//import lombok.NoArgsConstructor;
//import lombok.Setter;
//
//import java.math.BigDecimal;
//import java.time.Instant;
//@Entity @Table(name="erp_fee_payment",
// uniqueConstraints=@UniqueConstraint(columnNames={"upiTransactionId","domain","academicSession"}))
//@Getter @Setter @NoArgsConstructor @AllArgsConstructor
//
////        submitted payments, proof, UPI/reference, status and receipt number.
//public class FeePayment {
// @Id @GeneratedValue(strategy=GenerationType.IDENTITY) private Long id;
// @ManyToOne(fetch=FetchType.LAZY,optional=false) @JoinColumn(name="student_id") private Student student;
// @Column(nullable=false) private String domain;
// @Column(nullable=false) private String academicSession;
// @Column(nullable=false,precision=14,scale=2) private BigDecimal amount;
// @Column(length=100) private String upiTransactionId;
// @Column(nullable=false) private String paymentMethod;
// private String bankReference;
// @Column(unique=true) private String receiptNumber;
// private String screenshotPath;
// @Enumerated(EnumType.STRING) @Column(nullable=false) private FeePaymentStatus status=FeePaymentStatus.PENDING;
// private String rejectionReason;
// private String verifiedBy;
// private Instant submittedAt=Instant.now();
// private Instant verifiedAt;
//}
