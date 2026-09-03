package com.demoproject.Entity.FeesAcounts;

import com.demoproject.Entity.Student;
import jakarta.persistence.*;
import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

import java.math.BigDecimal;
@Entity @Table(name="erp_fee_account", uniqueConstraints=@UniqueConstraint(columnNames={"student_id","domain","academicSession"}))
@Getter @Setter @NoArgsConstructor @AllArgsConstructor

//total fees per student and academic session.
public class FeeAccount {
 @Id @GeneratedValue(strategy=GenerationType.IDENTITY) private Long id;
 @ManyToOne(fetch=FetchType.LAZY,optional=false) @JoinColumn(name="student_id") private Student student;
 @Column(nullable=false) private String domain;
 @Column(nullable=false) private String academicSession;
 @Column(nullable=false,precision=14,scale=2) private BigDecimal totalFees=BigDecimal.ZERO;
}
