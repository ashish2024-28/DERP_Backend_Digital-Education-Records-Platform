package com.demoproject.Repository;

import java.util.List;
import java.util.Optional;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import com.demoproject.Entity.Faculty;
import com.demoproject.Entity.University;

@Repository
public interface FacultyRepository extends JpaRepository<Faculty, Long> {

    // ---------------------------------------------------------
    // Find faculty by email
    // ---------------------------------------------------------
    Optional<Faculty> findByEmail(String email);


    // ---------------------------------------------------------
    // Find all faculty for a specific university/domain
    // ---------------------------------------------------------
    List<Faculty> findByDomain(String domain);


    // ---------------------------------------------------------
    // Find one faculty by database ID + domain
    // ---------------------------------------------------------
    Faculty findByIdAndDomain(Long id, String domain);


    // ---------------------------------------------------------
    // Find one faculty by Faculty ID + domain
    // ---------------------------------------------------------
    Faculty findByFacultyIdAndDomain(
            String facultyId,
            String domain
    );


    // ---------------------------------------------------------
    // Find one faculty by domain + email
    // ---------------------------------------------------------
    Faculty findByDomainAndEmail(
            String domain,
            String email
    );


    // ---------------------------------------------------------
    // Find one faculty by email + domain
    // ---------------------------------------------------------
    Optional<Faculty> findByEmailAndDomain(
            String email,
            String domain
    );


    // ---------------------------------------------------------
    // Login
    // ---------------------------------------------------------
    Faculty findByEmailAndPassword(
            String email,
            String password
    );


    // ---------------------------------------------------------
    // Find faculty by course + domain
    // ---------------------------------------------------------
    List<Faculty> findByCourseAndDomain(
            String course,
            String domain
    );


    // ---------------------------------------------------------
    // Find faculty assigned to a particular teaching batch
    //
    // Example teaching_assignments:
    //
    // {
    //     "2A": ["JAVA", "DSA"],
    //     "2B": ["OS"]
    // }
    //
    // PostgreSQL JSONB:
    // jsonb_exists(teaching_assignments, :batch)
    // ---------------------------------------------------------
    @Query(value = """
        SELECT *
        FROM faculty
        WHERE domain = :domain
          AND jsonb_exists(teaching_assignments, :batch)
        """, nativeQuery = true)
    List<Faculty> findByTeachingBatchAndDomain(
            @Param("batch") String batch,
            @Param("domain") String domain
    );


    // ---------------------------------------------------------
    // Existence checks
    // ---------------------------------------------------------
    boolean existsByEmail(String email);

    boolean existsByFacultyIdAndDomain(
            String facultyId,
            String domain
    );

    boolean existsByDomainAndEmail(
            String domain,
            String email
    );


    // ---------------------------------------------------------
    // Count
    // ---------------------------------------------------------
    long countByUniversity(University university);

    long countByUniversity_Domain(String domain);

    boolean existsByEmailIgnoreCase(String email);
}
