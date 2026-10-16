package com.demoproject.Entity.FeesAccounts;

import com.demoproject.Entity.Student;
import jakarta.persistence.*;
import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

@Entity
@Table(name="student_fee_account")
@Getter @Setter @NoArgsConstructor @AllArgsConstructor

//total fees , total paid , dues of per student with academic session .

public class StudentFeesAccount {

 @Id
 @GeneratedValue(strategy=GenerationType.IDENTITY)
 private Long id;

 @ManyToOne(fetch=FetchType.LAZY,optional=false)
 @JoinColumn(name="student_id")
 private Student student;


// @Column(nullable=false)
// private String academicSession;

 @Column(nullable=false)
 private Double totalFees;

 private Double totalPaid = 0.0;

}
