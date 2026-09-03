package com.demoproject.Entity;

import com.demoproject.Entity.ProfileInformation.StudentInfo.Certifications;
import com.fasterxml.jackson.annotation.JsonIgnore;

import jakarta.persistence.CascadeType;
import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.FetchType;
import jakarta.persistence.JoinColumn;
import jakarta.persistence.ManyToOne;
import jakarta.persistence.OneToMany;
import jakarta.persistence.Table;
import jakarta.persistence.UniqueConstraint;

import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

import org.hibernate.annotations.JdbcTypeCode;
import org.hibernate.type.SqlTypes;

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
     * Course.
     *
     * Examples:
     * B.Tech
     * BCA
     * B.Pharma
     */
    @Column(nullable = false)
    private String course;


    /*
     * Branch.
     *
     * Examples:
     * CSE
     * ECE
     * Mechanical
     */
    @Column(length = 50)
    private String branch;


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
     * Current study batch / section.
     *
     * Examples:
     * 2A
     * 2B
     * 3A
     * 4C
     */
    @Column(nullable = false, length = 20)
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
    @JdbcTypeCode(SqlTypes.JSON)
    @Column(
            name = "study_subjects",
            columnDefinition = "json"
    )
    private List<String> studySubjects = new ArrayList<>();


    /*
     * Student's father name.
     */
    @Column(nullable = false)
    private String fatherName;


    /*
     * Student's father mobile number.
     */
    @Column(nullable = false)
    private String fatherMobNo;


    /*
     * University / college.
     */
    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "university_id")
    @JsonIgnore
    private University university;


    /*
     * Student Certifications.
     */
    @OneToMany(
            mappedBy = "student",
            cascade = CascadeType.ALL,
            orphanRemoval = true
    )
    private List<Certifications> certifications =
            new ArrayList<>();


    // =========================================================
    // SUBJECT HELPER METHODS
    // =========================================================

    /**
     * Get student subjects safely.
     *
     * Example:
     *
     * ["JAVA", "DSA", "OS"]
     */
    public List<String> getStudySubjectsList() {

        if (studySubjects == null) {
            return new ArrayList<>();
        }

        return new ArrayList<>(studySubjects);
    }


    /**
     * Replace all student subjects.
     *
     * Example:
     *
     * setStudySubjectsList(
     *     List.of("JAVA", "DSA", "OS")
     * );
     */
    public void setStudySubjectsList(
            List<String> subjects) {

        if (subjects == null) {
            this.studySubjects =
                    new ArrayList<>();

            return;
        }

        List<String> normalizedSubjects =
                subjects.stream()
                        .filter(subject ->
                                subject != null &&
                                        !subject.isBlank()
                        )
                        .map(String::trim)
                        .map(String::toUpperCase)
                        .distinct()
                        .toList();

        this.studySubjects =
                new ArrayList<>(normalizedSubjects);
    }


    /**
     * Add one subject.
     *
     * Example:
     *
     * student.addStudySubject("JAVA");
     */
    public void addStudySubject(
            String subjectCode) {

        if (subjectCode == null ||
                subjectCode.isBlank()) {

            return;
        }

        if (this.studySubjects == null) {
            this.studySubjects =
                    new ArrayList<>();
        }

        String normalizedSubject =
                subjectCode
                        .trim()
                        .toUpperCase();

        boolean alreadyExists =
                this.studySubjects.stream()
                        .anyMatch(subject ->
                                subject != null &&
                                        subject.equalsIgnoreCase(
                                                normalizedSubject
                                        ));

        if (!alreadyExists) {
            this.studySubjects.add(
                    normalizedSubject
            );
        }
    }


    /**
     * Remove one subject.
     *
     * Example:
     *
     * student.removeStudySubject("JAVA");
     */
    public void removeStudySubject(
            String subjectCode) {

        if (subjectCode == null ||
                subjectCode.isBlank() ||
                this.studySubjects == null) {

            return;
        }

        String normalizedSubject =
                subjectCode.trim();

        this.studySubjects.removeIf(
                subject ->
                        subject != null &&
                                subject.equalsIgnoreCase(
                                        normalizedSubject
                                )
        );
    }


    /**
     * Check whether student studies
     * a particular subject.
     */
    public boolean studiesSubject(
            String subjectCode) {

        if (subjectCode == null ||
                subjectCode.isBlank() ||
                this.studySubjects == null) {

            return false;
        }

        String normalizedSubject =
                subjectCode.trim();

        return this.studySubjects.stream()
                .anyMatch(subject ->
                        subject != null &&
                                subject.equalsIgnoreCase(
                                        normalizedSubject
                                ));
    }


    /**
     * Clear all subjects.
     */
    public void clearStudySubjects() {

        this.studySubjects =
                new ArrayList<>();
    }


    // =========================================================
    // BATCH HELPER METHODS
    // =========================================================

    /**
     * Set study batch safely.
     *
     * Example:
     *
     * 2a -> 2A
     */
    public void setStudyBatch(
            String studyBatch) {

        if (studyBatch == null ||
                studyBatch.isBlank()) {

            this.studyBatch =
                    studyBatch;

            return;
        }

        this.studyBatch =
                studyBatch
                        .trim()
                        .toUpperCase();
    }


    /**
     * Check whether student belongs
     * to a particular study batch.
     */
    public boolean isInStudyBatch(
            String studyBatch) {

        if (this.studyBatch == null ||
                studyBatch == null) {

            return false;
        }

        return this.studyBatch.equalsIgnoreCase(
                studyBatch.trim()
        );
    }
}


/*
Why this fixes your error

Previously you had:

@JdbcTypeCode(SqlTypes.JSON)
@Column(name = "study_subjects", columnDefinition = "json")
private List<String> studySubjects;

which is actually the right mapping.

But your helper methods were still trying to use an old field:

studySubjectsJson

The corrected entity has one source of truth:

private List<String> studySubjects;

and Hibernate handles the JSON conversion.

Your Excel flow:

Excel
  ↓
"JAVA,DSA,OS,DBMS"
  ↓
List<String>
  ↓
["JAVA","DSA","OS","DBMS"]
  ↓
Hibernate @JdbcTypeCode(SqlTypes.JSON)
  ↓
PostgreSQL json

So PostgreSQL receives JSON rather than a VARCHAR.

One more important thing

Your database column is currently:

study_subjects json

Keep it as json with the entity above.

You do not need to manually do:

ObjectMapper.writeValueAsString(...)

and you should remove these imports from Student.java:

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.core.type.TypeReference;
import com.fasterxml.jackson.databind.ObjectMapper;

The Excel upload code you showed earlier can remain as it is:

student.setStudySubjects(subjects);

That is now exactly what the entity expects.
* */