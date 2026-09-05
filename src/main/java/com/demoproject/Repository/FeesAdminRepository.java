package com.demoproject.Repository;

import com.demoproject.Entity.FeesAdmin;
import com.demoproject.Entity.University;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;


@Repository
public interface FeesAdminRepository extends JpaRepository<FeesAdmin, Long> {


    Optional<FeesAdmin> findByEmail(String email);

    List<FeesAdmin> findByDomain(String domain);

    // find by domain + id   or  READ ONE by domain + id
    // **** this is for official use only no others  ***** 
    FeesAdmin findByIdAndDomain(Long id, String domain);

    FeesAdmin findByFeesAdminIdAndDomain(String subAdminId, String domain);

    Optional<FeesAdmin> findByEmailAndDomain(String email, String domain);

    FeesAdmin findByDomainAndEmail(String domain, String email);

    FeesAdmin findByEmailAndPassword(String email, String password);


// check this is exist or not
 
    boolean existsByEmail(String email);
    boolean existsByFeesAdminIdAndDomain(String subAdminId, String domain);
    boolean existsByDomainAndEmail(String domain,String email);

    
    
    // count 
    long countByUniversity(University university);
    long countByUniversity_Domain(String domain);


    boolean existsByEmailIgnoreCase(String email);
}

