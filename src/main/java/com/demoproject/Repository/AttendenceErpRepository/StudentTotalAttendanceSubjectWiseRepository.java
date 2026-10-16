package com.demoproject.Repository.AttendenceErpRepository;

import com.demoproject.Entity.AttendenceErp.StudentTotalAttendanceSubjectWise;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;
import java.util.Optional;

public interface StudentTotalAttendanceSubjectWiseRepository
        extends JpaRepository<StudentTotalAttendanceSubjectWise, Long> {

 Optional<StudentTotalAttendanceSubjectWise>
 findByDomainIgnoreCaseAndStudent_IdAndSubject(
         String domain,
         Long studentId,
         String subject
 );

 List<StudentTotalAttendanceSubjectWise>
 findByStudent_Id(
         Long studentId
 );

 List<StudentTotalAttendanceSubjectWise>
 findByDomainAndStudent_Id(
         String domain,
         Long studentId
 );

 void deleteByDomain(String domain);
}