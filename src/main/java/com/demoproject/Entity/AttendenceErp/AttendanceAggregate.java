package com.demoproject.Entity.AttendenceErp;

import com.demoproject.Entity.Student;
import jakarta.persistence.*;
import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

@Entity
@Table(
        name = "erp_attendance_aggregate",
        uniqueConstraints = @UniqueConstraint(
                name = "uk_attendance_aggregate_student_subject",
                columnNames = {
                        "student_id",
                        "subject"
                }
        ),
        indexes = {
                @Index(
                        name = "idx_attendance_aggregate_domain_student",
                        columnList = "domain,student_id"
                )
        }
)
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
public class AttendanceAggregate {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(nullable = false)
    private String domain;

    @Column(nullable = false)
    private String subject;

    @Column(nullable = false)
    private long totalClasses = 0;

    @Column(nullable = false)
    private long presentCount = 0;

    @Column(nullable = false)
    private long absentCount = 0;

    @ManyToOne(
            fetch = FetchType.LAZY,
            optional = false
    )
    @JoinColumn(
            name = "student_id",
            nullable = false
    )
    private Student student;
}