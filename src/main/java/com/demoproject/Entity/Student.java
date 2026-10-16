package com.demoproject.Entity;

import com.demoproject.Entity.AttendenceErp.StudentAttendanceRecordDayWise;
import com.demoproject.Entity.AttendenceErp.StudentTotalAttendanceSubjectWise;
import com.demoproject.Entity.FeesAccounts.StudentFeesAccount;
import com.demoproject.Entity.FeesAccounts.StudentFeesPayment;
import com.demoproject.Entity.ProfileInformation.StudentInfo.Certifications;
import com.demoproject.annotation.TitleCase;
import com.demoproject.annotation.UpperCase;
import com.demoproject.listener.StringNormalizationListener;
import com.fasterxml.jackson.annotation.JsonIgnore;

import jakarta.persistence.*;

import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

import java.util.ArrayList;
import java.util.List;

@Entity
@Table(
        name = "students",
        uniqueConstraints = @UniqueConstraint(columnNames = "email")
)
@NoArgsConstructor
@AllArgsConstructor
@Getter
@Setter
public class Student extends BaseUser {

    /*
     * Student roll number.
     *
     * Examples:
     * 101
     * 102
     * CSE2026001
     */
    @Column(nullable = false)
    private String rollNumber;


    /*
     * Course + Branch.

     * Examples: B.Tech CSE, B.TECH CSE AI , B.TECH CSE DS , BCA, B.Pharma
      */
    @Column(nullable = false)
    @UpperCase
    private String course;


    /*
     * Admission / academic batch.
     *
     * Examples:
     * 2024-28
     * 2025-29
     */
    @Column(nullable = false)
    private String batch;

    /*
     * Admission / academic session.
     *
     * Examples:
     * 2024-25
     * 2025-26
     */
    @Column(nullable=false)
    private String academicSession;

    /*
     * Current study batch / section.
     *
     * Examples:
     * 2A
     * 2B
     * 3A
     * 4C
     */
    @Column(nullable = false, length = 20)
    @UpperCase
    private String studyBatch;



    /*
     * Subjects currently studied by the student.
     *
     * PostgreSQL:
     *
     * study_subjects JSON
     *
     * Example stored JSON:
     *
     * [
     *   "JAVA",
     *   "DSA",
     *   "OS",
     *   "DBMS"
     * ]
     *
     * Hibernate 6 automatically converts:
     *
     * Java List<String>
     *          ↓
     * PostgreSQL JSON
     */
    @Column(name = "study_subjects",
            nullable = false
    )
    @UpperCase
    private String studySubjects;


    /*
     * Student's father name.
     */
    @Column(nullable = false)
    @TitleCase
    private String fatherName;


    /*
     * Student's father mobile number.
     */
    @Column(nullable = false)
    private String fatherMobNo;


    /*
     * Student Certifications.
     */
    @OneToMany(
            mappedBy = "student",
            cascade = CascadeType.ALL,
            orphanRemoval = true
    )
    private List<Certifications> certifications = new ArrayList<>();

    /*
     * Student FeesAccount.
     */
    @OneToMany(mappedBy = "student",cascade = CascadeType.ALL,orphanRemoval = true)
    private List<StudentFeesPayment> feesPayments = new ArrayList<>();

    @OneToOne(mappedBy = "student",cascade = CascadeType.ALL,orphanRemoval = true)
    private StudentFeesAccount feesAccount ;


    /*
     * Student Attendance.
     */

    @OneToMany(mappedBy = "student",cascade = CascadeType.ALL,orphanRemoval = true)
    private List<StudentAttendanceRecordDayWise>  studentAttendanceRecordDayWises= new ArrayList<>();

    @OneToMany(mappedBy = "student",cascade = CascadeType.ALL,orphanRemoval = true)
    private List<StudentTotalAttendanceSubjectWise> studentTotalAttendanceSubjectWise;




    /*
     * University / college.
     */
    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "university_id")
    @JsonIgnore
    private University university;







    public boolean studiesSubject(String subject) {

        if (studySubjects == null ||
                studySubjects.isBlank() ||
                subject == null ||
                subject.isBlank()) {

            return false;
        }

        String requestedSubject =
                subject.trim().toUpperCase();

        for (String studentSubject :
                studySubjects.split(",")) {

            if (studentSubject.trim()
                    .equalsIgnoreCase(requestedSubject)) {

                return true;
            }
        }

        return false;
    }

    public List<String> getStudySubjectsList() {

        List<String> subjects =
                new ArrayList<>();

        if (studySubjects == null ||
                studySubjects.isBlank()) {

            return subjects;
        }

        for (String subject :
                studySubjects.split(",")) {

            if (!subject.isBlank()) {

                subjects.add(
                        subject.trim().toUpperCase()
                );
            }
        }

        return subjects;
    }
}