package com.demoproject.Repository.AttendenceErpRepository;

import com.demoproject.Entity.AttendenceErp.AttendanceAggregate;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;
import java.util.Optional;

public interface AttendanceAggregateRepository
        extends JpaRepository<AttendanceAggregate, Long> {

 Optional<AttendanceAggregate>
 findByStudent_IdAndSubject(
         Long studentId,
         String subject
 );

 List<AttendanceAggregate>
 findByStudent_Id(
         Long studentId
 );

 List<AttendanceAggregate>
 findByDomainAndStudent_Id(
         String domain,
         Long studentId
 );

 void deleteByDomain(String domain);
}