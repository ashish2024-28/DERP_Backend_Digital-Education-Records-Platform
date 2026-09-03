package com.demoproject.Repository;

import java.util.List;
import java.util.Optional;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import com.demoproject.Entity.Student;
import com.demoproject.Entity.University;

@Repository
public interface StudentRepository extends JpaRepository<Student, Long> {

    Optional<Student> findByEmail(String email);

    // READ ALL students for specific university
    List<Student> findAllByDomain(String domain);

    // READ ONE by id + domain
    Student findByIdAndDomain(Long id, String domain);

    // Roll number + domain
    Optional<Student> findByRollNumberAndDomain(
            String rollNumber,
            String domain
    );

    // Email + domain
    Optional<Student> findByEmailAndDomain(
            String email,
            String domain
    );

    Student findByDomainAndEmail(
            String domain,
            String email
    );

    // Email + password
    Student findByEmailAndPassword(
            String email,
            String password
    );

    // Name + domain
    List<Student> findAllByNameAndDomain(
            String name,
            String domain
    );

    // Branch + domain
    List<Student> findAllByBranchAndDomain(
            String branch,
            String domain
    );

    // Course + domain
    List<Student> findAllByCourseAndDomain(
            String course,
            String domain
    );

    // Batch + domain
    List<Student> findAllByBatchAndDomain(
            String batch,
            String domain
    );

    // Check existence
    boolean existsByEmail(String email);

    boolean existsByRollNumberAndDomain(
            String rollNumber,
            String domain
    );

    boolean existsByDomainAndEmail(
            String domain,
            String email
    );

    // Count
    long countByUniversity(University university);

    long countByUniversity_Domain(String domain);

    // Faculty service calls
    List<Student> findByCourseAndDomain(
            String course,
            String domain
    );

    // Study batch
    List<Student> findByStudyBatchAndDomain(
            String studyBatch,
            String domain
    );

    // Study batch + roll number ordering
    List<Student> findByDomainAndStudyBatchOrderByRollNumberAsc(
            String domain,
            String studyBatch
    );

    // All students ordered by roll number
    List<Student> findByDomainOrderByRollNumberAsc(
            String domain
    );

    boolean existsByEmailIgnoreCase(String email);
}