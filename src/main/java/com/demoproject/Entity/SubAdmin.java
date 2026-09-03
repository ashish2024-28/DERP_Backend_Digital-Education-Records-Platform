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
        name = "sub_admin",
        uniqueConstraints = @UniqueConstraint(columnNames = "email")
)
@AllArgsConstructor
@NoArgsConstructor
@Getter
@Setter
public class SubAdmin extends BaseUser {

    /*
     * SubAdmin ID
     *
     * Example:
     * SUB001
     * SUB002
     */
    @Column(nullable = false)
    private String subAdminId;

    /*
     * Course managed by SubAdmin
     *
     * Example:
     * B.Tech
     * BCA
     * B.Pharma
     */
    @Column(nullable = false)
    private String course;

    /*
     * Multiple batches with multiple subjects.
     *
     * Example JSON:
     *
     * {
     *   "1A": ["JAVA", "C", "DSA"],
     *   "2A": ["AI", "ML", "OS"],
     *   "2C": ["OS", "AI"],
     *   "3A": ["MATH"]
     * }
     */
    @Column(
            name = "teaching_assignments",
            columnDefinition = "json",
            nullable = false
    )
    @JdbcTypeCode(SqlTypes.JSON)
    private String teachingAssignmentsJson = "{}";

    /*
     * University assigned to this SubAdmin.
     */
    @ManyToOne
    @JoinColumn(name = "university_id")
    @JsonIgnore
    private University university;

    /*
     * ObjectMapper used for JSON conversion.
     */
    private static final ObjectMapper OBJECT_MAPPER =
            new ObjectMapper();


    // =========================================================
    // GET TEACHING ASSIGNMENTS AS MAP
    // =========================================================

    public Map<String, List<String>> getTeachingAssignmentsMap() {

        if (teachingAssignmentsJson == null ||
                teachingAssignmentsJson.isBlank()) {

            return new LinkedHashMap<>();
        }

        try {

            return OBJECT_MAPPER.readValue(
                    teachingAssignmentsJson,
                    new TypeReference<
                            Map<String, List<String>>
                            >() {}
            );

        } catch (JsonProcessingException e) {

            throw new IllegalStateException(
                    "Invalid teaching assignments JSON",
                    e
            );
        }
    }


    // =========================================================
    // SET TEACHING ASSIGNMENTS
    // =========================================================

    public void setTeachingAssignmentsMap(
            Map<String, List<String>> assignments) {

        if (assignments == null) {
            assignments = new LinkedHashMap<>();
        }

        /*
         * LinkedHashMap keeps batch order.
         */
        Map<String, List<String>> normalized =
                new LinkedHashMap<>();

        assignments.forEach((batch, subjects) -> {

            if (batch == null ||
                    batch.isBlank()) {

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
                            .collect(
                                    java.util.stream.Collectors
                                            .toList()
                            );

            normalized.put(
                    normalizedBatch,
                    new ArrayList<>(
                            normalizedSubjects
                    )
            );
        });

        try {

            this.teachingAssignmentsJson =
                    OBJECT_MAPPER.writeValueAsString(
                            normalized
                    );

        } catch (JsonProcessingException e) {

            throw new IllegalStateException(
                    "Unable to save teaching assignments",
                    e
            );
        }
    }


    // =========================================================
    // ADD TEACHING ASSIGNMENT
    // =========================================================

    public void addTeachingAssignment(
            String batch,
            String subject) {

        if (batch == null ||
                batch.isBlank() ||
                subject == null ||
                subject.isBlank()) {

            throw new IllegalArgumentException(
                    "Batch and subject are required"
            );
        }

        Map<String, List<String>> assignments =
                getTeachingAssignmentsMap();

        String normalizedBatch =
                batch.trim().toUpperCase();

        String normalizedSubject =
                subject.trim().toUpperCase();

        List<String> subjects =
                assignments.computeIfAbsent(
                        normalizedBatch,
                        key -> new ArrayList<>()
                );

        if (!subjects.contains(
                normalizedSubject)) {

            subjects.add(
                    normalizedSubject
            );
        }

        setTeachingAssignmentsMap(
                assignments
        );
    }


    // =========================================================
    // REMOVE TEACHING ASSIGNMENT
    // =========================================================

    public void removeTeachingAssignment(
            String batch,
            String subject) {

        if (batch == null ||
                batch.isBlank() ||
                subject == null ||
                subject.isBlank()) {

            return;
        }

        Map<String, List<String>> assignments =
                getTeachingAssignmentsMap();

        String normalizedBatch =
                batch.trim().toUpperCase();

        String normalizedSubject =
                subject.trim().toUpperCase();

        List<String> subjects =
                assignments.get(
                        normalizedBatch
                );

        if (subjects != null) {

            subjects.removeIf(
                    existingSubject ->
                            existingSubject.equalsIgnoreCase(
                                    normalizedSubject
                            )
            );

            /*
             * Remove batch if it has no subjects.
             */
            if (subjects.isEmpty()) {

                assignments.remove(
                        normalizedBatch
                );
            }
        }

        setTeachingAssignmentsMap(
                assignments
        );
    }


    // =========================================================
    // CHECK WHETHER SUBADMIN MANAGES SUBJECT
    // =========================================================

    public boolean manages(
            String batch,
            String subject) {

        if (batch == null ||
                subject == null) {

            return false;
        }

        String normalizedBatch =
                batch.trim().toUpperCase();

        String normalizedSubject =
                subject.trim().toUpperCase();

        List<String> subjects =
                getTeachingAssignmentsMap()
                        .get(normalizedBatch);

        if (subjects == null) {
            return false;
        }

        return subjects.stream()
                .anyMatch(existingSubject ->
                        existingSubject.equalsIgnoreCase(
                                normalizedSubject
                        )
                );
    }
}