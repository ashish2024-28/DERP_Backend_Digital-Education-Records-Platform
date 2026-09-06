package com.demoproject.Entity.AttendenceErp;

import jakarta.persistence.*;
import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

import java.time.Instant;

@Entity
@Table(
        name = "erp_attendance_retention_config",
        uniqueConstraints = @UniqueConstraint(
                name = "uk_attendance_retention_domain",
                columnNames = "domain"
        )
)
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
public class AttendanceRetentionConfig {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(
            nullable = false,
            unique = true
    )
    private String domain;

    @Column(nullable = false)
    private Integer retentionMonths = 12;

    @Column(nullable = false)
    private Boolean enabled = true;

    @Column(nullable = false)
    private Instant updatedAt = Instant.now();
}