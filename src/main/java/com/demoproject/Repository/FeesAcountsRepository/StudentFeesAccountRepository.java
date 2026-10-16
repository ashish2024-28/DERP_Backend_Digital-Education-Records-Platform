package com.demoproject.Repository.FeesAcountsRepository;

import com.demoproject.Entity.FeesAccounts.StudentFeesAccount;
import java.util.Optional;
import jakarta.persistence.LockModeType;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Lock;

public interface StudentFeesAccountRepository extends JpaRepository<StudentFeesAccount,Long> {

    @Lock(LockModeType.PESSIMISTIC_WRITE)
    Optional<StudentFeesAccount> findByStudentId(Long studentId);
}
