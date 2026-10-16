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


    @ManyToOne
    @JoinColumn(name = "university_id")
    @JsonIgnore
    private University university;



}