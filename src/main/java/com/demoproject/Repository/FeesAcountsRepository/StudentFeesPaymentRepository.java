package com.demoproject.Repository.FeesAcountsRepository;

import com.demoproject.Entity.FeesAccounts.StudentFeesPayment;
import com.demoproject.Entity.Student;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.Optional;
import java.util.List;
import jakarta.persistence.LockModeType;
import org.springframework.data.jpa.repository.Lock;

public interface StudentFeesPaymentRepository extends JpaRepository<StudentFeesPayment, Long> {


   boolean existsByUpiTransactionId(String upiTransactionId);

   List<StudentFeesPayment> findByStudentOrderByIdDesc(Student student);

   List<StudentFeesPayment> findByStudent_DomainAndStatusOrderByIdDesc(
           String domain, com.demoproject.Entity.FeesAccounts.FeesPaymentStatus status);
   List<StudentFeesPayment> findByStudent_DomainOrderByIdDesc(String domain);
   long countByStudent_Domain(String domain);

   @Lock(LockModeType.PESSIMISTIC_WRITE)
   Optional<StudentFeesPayment> findByUpiTransactionIdAndStudent(String upiTransactionId, Student student);
}
