package com.demoproject.Service.FeesAcountsService;

import com.demoproject.DTO.FeesAccount.StudentFeesAccountDTO;
import com.demoproject.DTO.FeesAccount.StudentFeesPaymentDTO;
import com.demoproject.DTO.FeesAccount.StudentFeesPaymentResponseDTO;
import com.demoproject.Entity.FeesAccounts.FeesPaymentMethodType;
import com.demoproject.Entity.FeesAccounts.StudentFeesAccount;
import com.demoproject.Entity.FeesAccounts.StudentFeesPayment;
import com.demoproject.Entity.Student;
import com.demoproject.Repository.FeesAcountsRepository.StudentFeesAccountRepository;
import com.demoproject.Repository.FeesAcountsRepository.StudentFeesPaymentRepository;
import com.demoproject.Repository.StudentRepository;
import lombok.RequiredArgsConstructor;
import org.modelmapper.ModelMapper;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;

@Service
@RequiredArgsConstructor

public class StudentAccountFeesService {

    private final StudentFeesPaymentRepository studentFeesPaymentRepository;
    private final StudentFeesAccountRepository studentFeesAccountRepository;

    private final StudentRepository studentRepository;

    private final ModelMapper modelMapper;


//  pay add payment online
    @Transactional
    public String studentAddPayment(String domain , String email , StudentFeesPaymentDTO studentFeesPaymentDTO){

        Student student = studentRepository.findByEmailAndDomain(email , domain)
                .orElseThrow(() ->new RuntimeException( "Student not found" ) );

        requirePositiveAmount(studentFeesPaymentDTO.getAmount());
        StudentFeesAccount account = studentFeesAccountRepository.findByStudentId(student.getId())
                .orElseThrow(() -> new RuntimeException("Student fee account not found"));
        String transactionId = studentFeesPaymentDTO.getUpiTransactionId().trim();
        if (studentFeesPaymentRepository.existsByUpiTransactionId(transactionId)) {
            throw new RuntimeException("Transaction reference has already been submitted");
        }

        StudentFeesPayment studentFeesPayment = new StudentFeesPayment();
        studentFeesPayment.setStudent(student);
        studentFeesPayment.setAcademicSession(student.getAcademicSession());
        studentFeesPayment.setAmount(studentFeesPaymentDTO.getAmount());
        studentFeesPayment.setUpiTransactionId(transactionId);
        studentFeesPayment.setPaymentType(FeesPaymentMethodType.valueOf(
                studentFeesPaymentDTO.getPaymentType().trim().toUpperCase()));
        studentFeesPayment.setStatus(com.demoproject.Entity.FeesAccounts.FeesPaymentStatus.PENDING);
        studentFeesPayment.setRejectionReason(null);
        studentFeesPaymentRepository.save(studentFeesPayment);

        return "Payment Added Successfully ";
    }

//  Student Fees Account
    @Transactional(readOnly = true)
    public StudentFeesAccountDTO studentFeesAccount(String domain , String email){

        Student student = studentRepository.findByEmailAndDomain(email , domain)
                .orElseThrow(() ->new RuntimeException( "Student not found" ) );

        StudentFeesAccount studentFeesAccount = studentFeesAccountRepository.findByStudentId(student.getId())
                .orElseThrow(() -> new RuntimeException("Student fee account not found"));
        StudentFeesAccountDTO response = modelMapper.map(studentFeesAccount, StudentFeesAccountDTO.class);
        double totalFees = zeroIfNull(studentFeesAccount.getTotalFees());
        double totalPaid = zeroIfNull(studentFeesAccount.getTotalPaid());
        response.setAcademicSession(student.getAcademicSession());
        response.setTotalFees(totalFees);
        response.setTotalPaid(totalPaid);
        response.setTotalDues(totalFees - totalPaid);
        return response;
    }

//  Student all Fees Payment history
    @Transactional(readOnly = true)
    public List<StudentFeesPaymentResponseDTO> studentFeesPayment(String domain , String email){

        Student student = studentRepository.findByEmailAndDomain(email , domain)
                .orElseThrow(() ->new RuntimeException( "Student not found" ) );

        return studentFeesPaymentRepository.findByStudentOrderByIdDesc(student).stream()
                .map(payment -> {
                    StudentFeesPaymentResponseDTO response =
                            modelMapper.map(payment, StudentFeesPaymentResponseDTO.class);
                    if (payment.getVerifyByFeesAdmin() != null) {
                        response.setVerifyByFeesAdmin(payment.getVerifyByFeesAdmin().getName());
                    }
                    return response;
                }).toList();
    }

    private void requirePositiveAmount(Double amount) {
        if (amount == null || !Double.isFinite(amount) || amount <= 0) {
            throw new RuntimeException("Payment amount must be a positive number");
        }
    }

    private double zeroIfNull(Double amount) {
        return amount == null ? 0.0 : amount;
    }

}
