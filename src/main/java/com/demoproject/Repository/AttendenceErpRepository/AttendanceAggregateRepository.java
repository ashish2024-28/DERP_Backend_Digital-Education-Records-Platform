package com.demoproject.Repository.AttendenceErpRepository;

import com.demoproject.Entity.AttendenceErp.AttendanceAggregate;

import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;
import java.util.Optional;

public interface AttendanceAggregateRepository
        extends JpaRepository<AttendanceAggregate, Long> {

 Optional<AttendanceAggregate>
 findByStudentRollNumberAndSubjectAndAcademicSession(
         String rollNO,
         String subject,
         String academicSession
 );

 List<AttendanceAggregate>
 findByStudentRollNumberOrderBySubjectAsc(
         String rollNO
 );

 List<AttendanceAggregate>
 findByDomainOrderByStudentIdAsc(
         String domain
 );
}