package com.demoproject.Repository.FeesAcountsRepository;

import com.demoproject.Entity.FeesAccounts.FeesCourseWise;
import com.demoproject.Entity.University;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;
import java.util.Optional;

public interface FeesCourseWiseRepository extends JpaRepository<FeesCourseWise, Long> {

    List<FeesCourseWise> findByUniversity(University university);

   Optional<FeesCourseWise> findByUniversityAndCourseIgnoreCase(University university, String course);

    long countByUniversity(University university);
}
