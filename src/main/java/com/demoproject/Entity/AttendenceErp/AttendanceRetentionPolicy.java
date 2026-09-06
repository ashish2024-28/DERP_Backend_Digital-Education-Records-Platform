//package com.demoproject.Entity.AttendenceErp;
//
//import jakarta.persistence.*;
//import lombok.AllArgsConstructor;
//import lombok.Getter;
//import lombok.NoArgsConstructor;
//import lombok.Setter;
//
//@Entity
//@Table(
//        name = "erp_attendance_retention_policy",
//        uniqueConstraints = @UniqueConstraint(
//                name = "uk_attendance_retention_domain",
//                columnNames = "domain"
//        )
//)
//@Getter
//@Setter
//@NoArgsConstructor
//@AllArgsConstructor
//public class AttendanceRetentionPolicy {
//
//    @Id
//    @GeneratedValue(strategy = GenerationType.IDENTITY)
//    private Long id;
//
//    @Column(nullable = false, length = 100)
//    private String domain;
//
//    /** Number of complete months of semester history to retain. */
//    @Column(nullable = false)
//    private int retentionMonths = 6;
//
//    /** First month of semester 1. 7 means Jul-Dec / Jan-Jun. */
//    @Column(nullable = false)
//    private int semesterStartMonth = 7;
//}
