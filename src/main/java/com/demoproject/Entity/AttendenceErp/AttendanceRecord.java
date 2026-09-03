package com.demoproject.Entity.AttendenceErp;

import com.demoproject.Entity.Faculty;
import com.demoproject.Entity.Student;

import jakarta.persistence.*;

import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

import java.time.LocalDate;

@Entity
@Table(
        name = "erp_attendance_record",
        uniqueConstraints = @UniqueConstraint(
                columnNames = {
                        "student_id",
                        "subject",
                        "attendance_date",
                        "period_number",
                        "academic_session"
                }
        ),
        indexes = {
                @Index(
                        name = "idx_attendance_student_date",
                        columnList = "student_id,attendance_date"
                ),
                @Index(
                        name = "idx_attendance_batch_date",
                        columnList = "domain,study_batch,attendance_date"
                )
        }
)
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
public class AttendanceRecord {

 @Id
 @GeneratedValue(strategy = GenerationType.IDENTITY)
 private Long id;

 @Column(nullable = false)
 private String domain;

 @Column(nullable = false)
 private String academicSession;

 @Column(nullable = false)
 private String teachingBatch;

 @Column(nullable = false)
 private String studyBatch;

 @Column(nullable = false)
 private String subject;

 @Column(
         name = "attendance_date",
         nullable = false
 )
 private LocalDate attendanceDate;

 /*
  * Period number.
  *
  * 1 = first period
  * 2 = second period
  * 3 = third period
  */
 @Column(
         name = "period_number",
         nullable = false
 )
 private Integer periodNumber;

 @Enumerated(EnumType.STRING)
 @Column(nullable = false)
 private AttendanceStatus status;

 @ManyToOne(
         fetch = FetchType.LAZY,
         optional = false
 )
 @JoinColumn(name = "student_id")
 private Student student;

 @ManyToOne(fetch = FetchType.LAZY)
 @JoinColumn(name = "marked_by_faculty_id")
 private Faculty markedBy;
}