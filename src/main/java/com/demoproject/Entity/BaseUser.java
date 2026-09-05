package com.demoproject.Entity;


import java.time.LocalDateTime;

import com.demoproject.annotation.LowerCase;
import com.demoproject.annotation.TitleCase;
import com.fasterxml.jackson.annotation.JsonProperty;

import jakarta.persistence.*;
import lombok.Data;

@MappedSuperclass
@Data
public abstract class BaseUser {
    // All fiels are commom for Student, Faculty, SubAdmin, DomainAdmin.
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(nullable = false)
    @LowerCase
    private String domain; // domain => university/collage ka unique name(like Haridwar University HU,hu,Hu)

    @Column(nullable = false)
    @TitleCase
    private String name;

    @Column(nullable = false , unique = true)
    private String mobileNumber; //Country code +91

    @Column(nullable = false, unique = true)
    @LowerCase
    private String email;

    @Column(nullable = false)
    @JsonProperty(access = JsonProperty.Access.WRITE_ONLY)
    private String password;

    @Enumerated(EnumType.STRING)
    private Role role;

    private LocalDateTime createdDateTime = LocalDateTime.now();
    private LocalDateTime lastUpdateDateTime;
    private LocalDateTime lastLoginDateTime; // For login purposes


    private String profilePic;


    @PrePersist
    public void prePersist(){
        createdDateTime = LocalDateTime.now();
        lastUpdateDateTime = LocalDateTime.now();
    }
    @PreUpdate
    public void preUpdata(){
        lastUpdateDateTime = LocalDateTime.now();
    }

    
}

