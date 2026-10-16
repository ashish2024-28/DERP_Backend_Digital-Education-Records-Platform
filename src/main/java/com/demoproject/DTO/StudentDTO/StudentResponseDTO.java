package com.demoproject.DTO.StudentDTO;

import com.demoproject.DTO.FeesAccount.StudentFeesAccountDTO;
import com.demoproject.DTO.FeesAccount.StudentFeesPaymentResponseDTO;
import com.demoproject.Entity.AttendenceErp.StudentAttendanceRecordDayWise;
import com.demoproject.Entity.AttendenceErp.StudentTotalAttendanceSubjectWise;
import com.demoproject.Entity.ProfileInformation.StudentInfo.Certifications;
import jakarta.persistence.Column;
import lombok.*;

import java.time.Instant;
import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;

@Data
@AllArgsConstructor
@NoArgsConstructor
public class StudentResponseDTO {

    private String name;

    private String email;

    private String mobileNumber;

    private LocalDateTime createdDateTime;

    private LocalDateTime lastUpdateDateTime;

    private LocalDateTime lastLoginDateTime;

    private String profilePic;

    private String role;




    private String rollNumber;

    private String course;

    private String batch;

    private String academicSession;

    private String studyBatch;

    private String studySubjects;

    private String fatherName;

    private String fatherMobNo;


    //    Fees
//    private List<Certifications> certifications ;

    //    Fees
//    private List<StudentFeesPaymentResponseDTO> feesPayments ;
    private StudentFeesAccountDTO feesAccount ;


//    Attendance
//    private List<StudentAttendanceRecordDayWise>  studentAttendanceRecordDayWises ;
//    private StudentTotalAttendanceSubjectWise studentTotalAttendanceSubjectWise;

//    university
//    private String university;







}