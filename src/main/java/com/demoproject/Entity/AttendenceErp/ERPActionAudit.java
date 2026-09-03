package com.demoproject.Entity.AttendenceErp;

import jakarta.persistence.*;

import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

import java.time.LocalDateTime;

@Entity
@Table(
        name = "erp_action_audit",
        indexes = {
                @Index(
                        name = "idx_audit_domain",
                        columnList = "domain"
                ),
                @Index(
                        name = "idx_audit_created",
                        columnList = "created_at"
                )
        }
)
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
public class ERPActionAudit {

 @Id
 @GeneratedValue(strategy = GenerationType.IDENTITY)
 private Long id;

 @Column(nullable = false)
 private String domain;

 @Column(nullable = false)
 private String actorEmail;

 @Column(nullable = false)
 private String role;

 @Column(nullable = false)
 private String action;

 private String targetType;

 private Long targetId;

 @Column(length = 2000)
 private String details;

 @Column(
         name = "created_at",
         nullable = false
 )
 private LocalDateTime createdAt =
         LocalDateTime.now();
}