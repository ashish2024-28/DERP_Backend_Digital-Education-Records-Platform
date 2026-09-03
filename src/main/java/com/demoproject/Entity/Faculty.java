package com.demoproject.Entity;

import com.fasterxml.jackson.annotation.JsonIgnore;
import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.core.type.TypeReference;
import com.fasterxml.jackson.databind.ObjectMapper;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.JoinColumn;
import jakarta.persistence.ManyToOne;
import jakarta.persistence.Table;
import jakarta.persistence.UniqueConstraint;

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
            columnDefinition = "json",
            nullable = false
    )
    @JdbcTypeCode(SqlTypes.JSON)
    private String teachingAssignmentsJson = "{}";

    @ManyToOne
    @JoinColumn(name = "university_id")
    @JsonIgnore
    private University university;


    // ---------------------------------------------------------
    // ObjectMapper
    // ---------------------------------------------------------

    private static final ObjectMapper OBJECT_MAPPER =
            new ObjectMapper();


    // ---------------------------------------------------------
    // Get assignments as Map
    // ---------------------------------------------------------

    public Map<String, List<String>> getTeachingAssignmentsMap() {

        if (teachingAssignmentsJson == null ||
                teachingAssignmentsJson.isBlank()) {

            return new LinkedHashMap<>();
        }

        try {

            return OBJECT_MAPPER.readValue(
                    teachingAssignmentsJson,
                    new TypeReference<Map<String, List<String>>>() {}
            );

        } catch (JsonProcessingException e) {

            throw new IllegalStateException(
                    "Invalid teachingAssignments JSON",
                    e
            );
        }
    }


    // ---------------------------------------------------------
    // Set assignments from Map
    // ---------------------------------------------------------

    public void setTeachingAssignmentsMap(
            Map<String, List<String>> assignments) {

        if (assignments == null) {
            assignments = new LinkedHashMap<>();
        }

        Map<String, List<String>> normalized =
                new LinkedHashMap<>();

        assignments.forEach((batch, subjects) -> {

            if (batch == null || batch.isBlank()) {
                return;
            }

            String normalizedBatch =
                    batch.trim().toUpperCase();

            List<String> normalizedSubjects =
                    subjects == null
                            ? new ArrayList<>()
                            : subjects.stream()
                            .filter(s ->
                                    s != null &&
                                            !s.isBlank())
                            .map(String::trim)
                            .map(String::toUpperCase)
                            .distinct()
                            .toList();

            normalized.put(
                    normalizedBatch,
                    new ArrayList<>(normalizedSubjects)
            );
        });

        try {

            this.teachingAssignmentsJson =
                    OBJECT_MAPPER.writeValueAsString(
                            normalized
                    );

        } catch (JsonProcessingException e) {

            throw new IllegalStateException(
                    "Unable to convert teaching assignments to JSON",
                    e
            );
        }
    }


    // ---------------------------------------------------------
    // Add assignment
    // ---------------------------------------------------------

    public void addTeachingAssignment(
            String batch,
            String subjectCode) {

        if (batch == null ||
                batch.isBlank() ||
                subjectCode == null ||
                subjectCode.isBlank()) {

            throw new IllegalArgumentException(
                    "Batch and subject are required"
            );
        }

        Map<String, List<String>> assignments =
                getTeachingAssignmentsMap();

        String normalizedBatch =
                batch.trim().toUpperCase();

        String normalizedSubject =
                subjectCode.trim().toUpperCase();

        List<String> subjects =
                assignments.computeIfAbsent(
                        normalizedBatch,
                        key -> new ArrayList<>()
                );

        if (!subjects.contains(normalizedSubject)) {
            subjects.add(normalizedSubject);
        }

        setTeachingAssignmentsMap(assignments);
    }


    // ---------------------------------------------------------
    // Remove assignment
    // ---------------------------------------------------------

    public void removeTeachingAssignment(
            String batch,
            String subjectCode) {

        if (batch == null ||
                batch.isBlank() ||
                subjectCode == null ||
                subjectCode.isBlank()) {

            return;
        }

        Map<String, List<String>> assignments =
                getTeachingAssignmentsMap();

        String normalizedBatch =
                batch.trim().toUpperCase();

        String normalizedSubject =
                subjectCode.trim().toUpperCase();

        List<String> subjects =
                assignments.get(normalizedBatch);

        if (subjects != null) {

            subjects.removeIf(subject ->
                    subject.equalsIgnoreCase(
                            normalizedSubject
                    ));

            if (subjects.isEmpty()) {
                assignments.remove(normalizedBatch);
            }
        }

        setTeachingAssignmentsMap(assignments);
    }


    // ---------------------------------------------------------
    // Check whether faculty teaches subject in batch
    // ---------------------------------------------------------

    public boolean teaches(
            String batch,
            String subjectCode) {

        if (batch == null ||
                subjectCode == null) {

            return false;
        }

        List<String> subjects =
                getTeachingAssignmentsMap()
                        .get(batch.trim().toUpperCase());

        if (subjects == null) {
            return false;
        }

        return subjects.stream()
                .anyMatch(subject ->
                        subject.equalsIgnoreCase(
                                subjectCode.trim()
                        ));
    }
}