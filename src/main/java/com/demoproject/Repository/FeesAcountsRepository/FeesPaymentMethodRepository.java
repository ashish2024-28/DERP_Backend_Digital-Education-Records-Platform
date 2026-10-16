package com.demoproject.Repository.FeesAcountsRepository;

import com.demoproject.Entity.FeesAccounts.FeesPaymentMethod;

import java.util.List;
import java.util.Optional;
import org.springframework.data.jpa.repository.JpaRepository;

public interface FeesPaymentMethodRepository extends JpaRepository<FeesPaymentMethod,Long> {

    List<FeesPaymentMethod> findByFeesAdminDomainAndActiveTrueOrderByIdDesc(String domain);
    List<FeesPaymentMethod> findByFeesAdminDomainOrderByIdDesc(String domain);
    List<FeesPaymentMethod> findByFeesAdminEmailAndFeesAdminDomainOrderByIdDesc(String email, String domain);
    Optional<FeesPaymentMethod> findByIdAndFeesAdminEmailAndFeesAdminDomain(Long id, String email, String domain);

    Optional<FeesPaymentMethod> findByIdAndFeesAdminDomain(Long methodId, String domain);
}
