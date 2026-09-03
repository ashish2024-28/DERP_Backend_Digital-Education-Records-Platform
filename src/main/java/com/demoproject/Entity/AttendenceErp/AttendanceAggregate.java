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
                columnNames = {
                        "student_id",
                        "subject",
                        "academic_session"
                }
        )
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
    private String academicSession;

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
    @JoinColumn(name = "student_id")
    private Student student;
}