package com.demoproject.DTO.StudentDTO;

import com.demoproject.DTO.FeesAccount.StudentFeesAccountDTO;
import com.demoproject.DTO.FeesAccount.StudentFeesPaymentResponseDTO;
import com.demoproject.DTO.CertificationResponseDTO;
import com.demoproject.DTO.AttendenceErp.StudentAttendanceDayResponseDTO;
import com.demoproject.DTO.AttendenceErp.StudentAttendanceSubjectResponseDTO;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.LocalDateTime;
import java.util.List;

@Data
@AllArgsConstructor
@NoArgsConstructor
public class StudentResponseWithProfileDTO {

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


    //    Certifications
    private List<CertificationResponseDTO> certifications ;

    //    Fees
    private List<StudentFeesPaymentResponseDTO> feesPayments ;
    private StudentFeesAccountDTO feesAccount ;


   //    Attendance
    private List<StudentAttendanceDayResponseDTO> studentAttendanceRecordDayWises;
    private List<StudentAttendanceSubjectResponseDTO> studentTotalAttendanceSubjectWise;

   //    university
//    private String universityName;







}