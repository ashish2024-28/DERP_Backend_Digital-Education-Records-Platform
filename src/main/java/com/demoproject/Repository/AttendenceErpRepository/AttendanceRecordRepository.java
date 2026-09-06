package com.demoproject.Repository.AttendenceErpRepository;

import com.demoproject.Entity.AttendenceErp.AttendanceRecord;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Modifying;
import org.springframework.data.jpa.repository.Query;

import java.time.LocalDate;
import java.util.List;
import java.util.Optional;

public interface AttendanceRecordRepository
        extends JpaRepository<AttendanceRecord, Long> {

 Optional<AttendanceRecord>
 findByStudent_IdAndSubjectAndAttendanceDateAndPeriodNumber(
         Long studentId,
         String subject,
         LocalDate attendanceDate,
         Integer periodNumber
 );

 List<AttendanceRecord>
 findByStudent_IdAndAttendanceDateGreaterThanEqualOrderByAttendanceDateDesc(
         Long studentId,
         LocalDate fromDate
 );

 List<AttendanceRecord>
 findByDomainAndStudyBatchAndAttendanceDateGreaterThanEqual(
         String domain,
         String studyBatch,
         LocalDate fromDate
 );

 List<AttendanceRecord>
 findByDomainOrderByAttendanceDateAsc(String domain);

 @Query("""
            SELECT DISTINCT a.domain
            FROM AttendanceRecord a
            WHERE a.domain IS NOT NULL
            """)
 List<String> findDistinctDomains();

 long deleteByDomainAndAttendanceDateBefore(
         String domain,
         LocalDate date
 );

 long deleteByDomainAndAttendanceDateBetween(
         String domain,
         LocalDate fromDate,
         LocalDate toDate
 );

 List<AttendanceRecord> findByDomainAndTeachingBatchAndSubjectAndAttendanceDateAndPeriodNumber(
         String domain,
         String teachingBatch,
         String subject,
         LocalDate attendanceDate,
         Integer periodNumber
 );

 List<AttendanceRecord> findByDomainAndMarkedBy_IdAndAttendanceDateGreaterThanEqualOrderByAttendanceDateDescPeriodNumberAsc(
         String domain,
         Long facultyId,
         LocalDate fromDate
 );
}