package com.demoproject.Entity;

import com.demoproject.annotation.UpperCase;
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



}