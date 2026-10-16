package com.demoproject.Repository.AttendenceErpRepository;

import com.demoproject.Entity.AttendenceErp.StudentAttendanceRecordDayWise;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;

import java.time.LocalDate;
import java.util.List;
import java.util.Optional;

public interface StudentAttendanceRecordDayWiseRepository
        extends JpaRepository<StudentAttendanceRecordDayWise, Long> {

 Optional<StudentAttendanceRecordDayWise>
 findByDomainIgnoreCaseAndStudent_IdAndSubjectIgnoreCaseAndAttendanceDateAndPeriodNumber(
         String domain,
         Long studentId,
         String subject,
         LocalDate attendanceDate,
         Integer periodNumber
 );

 List<StudentAttendanceRecordDayWise> findByStudent_Id(Long id);

 List<StudentAttendanceRecordDayWise>
 findByStudent_IdAndAttendanceDateGreaterThanEqualOrderByAttendanceDateDesc(
         Long studentId,
         LocalDate fromDate
 );



 List<StudentAttendanceRecordDayWise>
 findByDomainOrderByAttendanceDateAsc(String domain);

 Page<StudentAttendanceRecordDayWise>
 findByDomainAndAttendanceDateBetweenOrderByAttendanceDateDescPeriodNumberAsc(
         String domain, LocalDate fromDate, LocalDate toDate, Pageable pageable);

 @Query("""
            SELECT DISTINCT a.domain
            FROM StudentAttendanceRecordDayWise a
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

 long countByDomainAndAttendanceDateBetween(
         String domain, LocalDate fromDate, LocalDate toDate);

 List<StudentAttendanceRecordDayWise> findByDomainAndMarkedBy_IdAndAttendanceDateGreaterThanEqualOrderByAttendanceDateDescPeriodNumberAsc(
         String domain,
         Long facultyId,
         LocalDate fromDate
 );

 List<StudentAttendanceRecordDayWise> findByDomainAndSubjectAndAttendanceDateAndPeriodNumber(String domain, String normalizedSubject, LocalDate date, Integer periodNumber);
}