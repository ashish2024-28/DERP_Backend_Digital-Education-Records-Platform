package com.demoproject.Entity.AttendenceErp;

import com.demoproject.Entity.Faculty;
import com.demoproject.Entity.Student;
import jakarta.persistence.*;
import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

import java.time.LocalDate;
import java.time.LocalTime;

@Entity
@Table(
        uniqueConstraints = @UniqueConstraint(
                name = "uk_attendance_domain_student_subject_date_period",
                columnNames = {
                        "domain",
                        "student_id",
                        "subject",
                        "attendance_date",
                        "period_number"
                }
        )
)
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
public class StudentAttendanceRecordDayWise {

 @Id
 @GeneratedValue(strategy = GenerationType.IDENTITY)
 private Long id;

 @Column(nullable = false)
 private String domain;


 @Column(nullable = false)
 private String subject;

 @Column(
         name = "attendance_date",
         nullable = false
 )
 private LocalDate attendanceDate;

 private LocalTime attendanceTime;

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
 @JoinColumn(
         name = "student_id",
         nullable = false
 )
 private Student student;

 @ManyToOne(fetch = FetchType.LAZY)
 @JoinColumn(name = "marked_by_faculty_id")
 private Faculty markedBy;
}