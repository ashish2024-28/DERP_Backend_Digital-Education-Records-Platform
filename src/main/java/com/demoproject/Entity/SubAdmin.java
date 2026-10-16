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
    @UpperCase
    private String course;


    /*
     * University assigned to this SubAdmin.
     */
    @ManyToOne
    @JoinColumn(name = "university_id")
    @JsonIgnore
    private University university;



}