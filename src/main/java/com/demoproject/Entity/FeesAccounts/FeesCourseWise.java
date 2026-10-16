package com.demoproject.Entity.FeesAccounts;

import com.demoproject.Entity.University;
import jakarta.persistence.*;
import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

import java.time.LocalDateTime;

@Entity
@Table(
        name = "fee_course_wise",
        uniqueConstraints = {
                @UniqueConstraint(
                        name = "uk_fee_course_university",
                        columnNames = {
                                "course",
                                "university_id"
                        }
                )
        }
)
@Getter @Setter @NoArgsConstructor @AllArgsConstructor

//payment method types.
public class FeesCourseWise {

 @Id
 @GeneratedValue(strategy=GenerationType.IDENTITY)
 private Long id;

 @Column(nullable=false,  unique = true)
 private String course;

 @Column(nullable=false)
 private Double fees;

 private LocalDateTime createdAt = LocalDateTime.now();

 private LocalDateTime updatedAt = LocalDateTime.now();

 @PrePersist
 public void prePersist(){
  createdAt = LocalDateTime.now();
  updatedAt = LocalDateTime.now();
 }
 @PreUpdate
 public void preUpdata(){
  updatedAt = LocalDateTime.now();
 }



 @ManyToOne(fetch=FetchType.LAZY,optional=false)
 @JoinColumn(name="university_id")
 private University university;



}


/*Why?

Old:

course UNIQUE

means:

BCA → only one record in whole database

New:

course + university_id UNIQUE

means:

HU     + BCA → allowed
ABC    + BCA → allowed
XYZ    + BCA → allowed

but:

HU + BCA
HU + BCA

duplicate nahi hoga.
*/