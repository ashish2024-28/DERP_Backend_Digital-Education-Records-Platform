package com.demoproject.Repository.AttendenceErpRepository;

import com.demoproject.Entity.AttendenceErp.AttendanceRecord;

import org.springframework.data.jpa.repository.JpaRepository;

import java.time.LocalDate;
import java.util.List;
import java.util.Optional;

public interface AttendanceRecordRepository
        extends JpaRepository<AttendanceRecord, Long> {

 Optional<AttendanceRecord>
 findByStudentRollNumberAndSubjectAndAttendanceDateAndPeriodNumberAndAcademicSession(
         String rollNO,
         String subject,
         LocalDate attendanceDate,
         Integer periodNumber,
         String academicSession
 );

 List<AttendanceRecord>
 findByStudentRollNumberAndAttendanceDateGreaterThanEqualOrderByAttendanceDateDesc(
         String rollNo,
         LocalDate fromDate
 );

 List<AttendanceRecord>
 findByDomainAndStudyBatchAndAttendanceDateGreaterThanEqual(
         String domain,
         String studyBatch,
         LocalDate fromDate
 );

 void deleteByAttendanceDateBefore(
         LocalDate date
 );
}