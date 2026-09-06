package com.demoproject.Entity;

import com.demoproject.annotation.UpperCase;
import com.demoproject.listener.StringNormalizationListener;
import com.fasterxml.jackson.annotation.JsonIgnore;
import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.core.type.TypeReference;
import com.fasterxml.jackson.databind.ObjectMapper;

import jakarta.persistence.*;

import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

import org.hibernate.annotations.JdbcTypeCode;
import org.hibernate.type.SqlTypes;

import java.util.ArrayList;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;

@Entity
@Table(
        name = "faculty",
        uniqueConstraints = @UniqueConstraint(columnNames = "email")
)
@NoArgsConstructor
@AllArgsConstructor
@Getter
@Setter
@EntityListeners(StringNormalizationListener.class)
public class Faculty extends BaseUser {

    /*
     * Faculty ID provided by university/college.
     *
     * Example:
     * FAC001
     * FAC002
     */
    @Column(nullable = false)
    private String facultyId;

    /*
     * Example:
     * B.Tech
     * B.Pharma
     * BCA
     */
    @Column(nullable = false)
    @UpperCase
    private String course;

    /*
     * Teaching assignments stored as PostgreSQL JSON.
     *
     * Example:
     *
     * {
     *     "2A": ["JAVA", "DSA"],
     *     "2B": ["OS"],
     *     "3A": ["DSA", "JAVA"],
     *     "4C": ["AI"]
     * }
     *
     * Key   = teaching batch
     * Value = subjects taught in that batch
     */
    @Column(
            name = "teaching_assignments",
            nullable = false
    )
    @UpperCase
    private String teachingAssignments;

    @ManyToOne
    @JoinColumn(name = "university_id")
    @JsonIgnore
    private University university;



    // =====================================================
    // CHECK WHETHER FACULTY TEACHES BATCH + SUBJECT
    // =====================================================

    public boolean teaches( String batch, String subject ) {

        if (teachingAssignments == null ||
                teachingAssignments.isBlank() ||
                batch == null ||
                batch.isBlank() ||
                subject == null ||
                subject.isBlank()) {

            return false;
        }

        String requestedBatch =
                batch.trim().toUpperCase();

        String requestedSubject =
                subject.trim().toUpperCase();

        for (String assignment :
                teachingAssignments.split(";")) {

            if (assignment.isBlank()) {
                continue;
            }

            String[] parts =
                    assignment.split(":", 2);

            if (parts.length != 2) {
                continue;
            }

            String assignmentBatch =
                    parts[0].trim().toUpperCase();

            if (!assignmentBatch.equals(requestedBatch)) {
                continue;
            }

            for (String assignedSubject :
                    parts[1].split(",")) {

                if (assignedSubject.trim()
                        .equalsIgnoreCase(requestedSubject)) {

                    return true;
                }
            }
        }

        return false;
    }

}