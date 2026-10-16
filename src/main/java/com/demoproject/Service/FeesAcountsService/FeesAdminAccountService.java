package com.demoproject.Service.FeesAcountsService;

import com.demoproject.DTO.FeesAccount.*;
import com.demoproject.Entity.FeesAccounts.*;
import com.demoproject.Entity.FeesAdmin;
import com.demoproject.Entity.Student;
import com.demoproject.Repository.FeesAcountsRepository.FeesPaymentMethodRepository;
import com.demoproject.Repository.FeesAcountsRepository.StudentFeesAccountRepository;
import com.demoproject.Repository.FeesAcountsRepository.StudentFeesPaymentRepository;
import com.demoproject.Repository.FeesAdminRepository;
import com.demoproject.Repository.StudentRepository;
import com.demoproject.Service.StudentService;
import com.demoproject.Service.AttendenceErpService.ERPActionAuditService;
import lombok.RequiredArgsConstructor;
import org.modelmapper.ModelMapper;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.multipart.MultipartFile;

import javax.imageio.ImageIO;
import javax.imageio.ImageReader;
import javax.imageio.stream.ImageInputStream;
import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.time.LocalDateTime;
import java.util.List;
import java.util.Set;
import java.util.UUID;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor

public class FeesAdminAccountService {

//    Fees Payment Method for FeesAdmin
    private final FeesPaymentMethodRepository feesPaymentMethodRepository;
//    Total Fees Account of student
    private final StudentFeesAccountRepository studentFeesAccountRepository;
//    Fees Payment History of student
    private final StudentFeesPaymentRepository studentFeesPaymentRepository;

    private final StudentService studentService;

    private final FeesAdminRepository feesAdminRepository;
    private final StudentRepository studentRepository;
    private final ModelMapper modelMapper;
    private final ERPActionAuditService auditService;
    private final String UPLOAD_DIR = "uploads/UpiQR/";


    // =========================================================
    // CREATE BANK PAYMENT METHOD
    // =========================================================

    @Transactional
    public String createFeesPaymentBank(String domain, String email , FeesPaymentBankDTO paymentBankDTO){

        FeesAdmin feesAdmin = feesAdminRepository.findByEmailAndDomain(email ,domain)
                .orElseThrow(() ->new RuntimeException( "FeesAdmin not found" ) );

        FeesPaymentMethod feesPaymentMethod = modelMapper.map(paymentBankDTO, FeesPaymentMethod.class);

        feesPaymentMethod.setPaymentType(FeesPaymentMethodType.BANK);
        feesPaymentMethod.setFeesAdmin(feesAdmin);

        feesPaymentMethod = feesPaymentMethodRepository.save(feesPaymentMethod);
        auditService.log(domain, email, "FEES_ADMIN", "FEES_PAYMENT_METHOD_CREATED",
                "FEE_PAYMENT_METHOD", feesPaymentMethod.getId(),
                "Bank payment method created: " + feesPaymentMethod.getDisplayName());

        return "Bank payment details added successfully";
    }

    // Create Online payments details
    @Transactional(rollbackFor = Exception.class)
    public String createFeesPaymentOnline(String domain, String email , FeesPaymentOnlineDTO paymentOnlineDTO , MultipartFile qrImage) throws IOException {
        FeesAdmin feesAdmin = feesAdminRepository.findByEmailAndDomain(email ,domain)
                .orElseThrow(() ->new RuntimeException( "FeesAdmin not found" ) );

        Path savedQrImage = null;
        String qrImagePath = null;
        try {
            if (qrImage != null && !qrImage.isEmpty()) {
                String contentType = qrImage.getContentType();
                if (!"image/png".equalsIgnoreCase(contentType)
                        && !"image/jpeg".equalsIgnoreCase(contentType)) {
                    throw new IllegalArgumentException("QR image must be a PNG or JPEG file.");
                }
                if (qrImage.getSize() > 5L * 1024 * 1024) {
                    throw new IllegalArgumentException("QR image must be 5 MB or smaller.");
                }

                try (ImageInputStream imageInput = ImageIO.createImageInputStream(qrImage.getInputStream())) {
                    if (imageInput == null || !ImageIO.getImageReaders(imageInput).hasNext()) {
                        throw new IllegalArgumentException("The selected QR file is not a valid image.");
                    }
                    ImageReader imageReader = ImageIO.getImageReaders(imageInput).next();
                    try {
                        imageReader.setInput(imageInput, true, true);
                        int width = imageReader.getWidth(0);
                        int height = imageReader.getHeight(0);
                        if (width < 1 || height < 1 || (long) width * height > 16_000_000L) {
                            throw new IllegalArgumentException("QR image dimensions are too large.");
                        }
                        imageReader.read(0);
                    } finally {
                        imageReader.dispose();
                    }
                }

                String extension = "image/png".equalsIgnoreCase(contentType) ? ".png" : ".jpg";
                Path uploadDirectory = Paths.get(UPLOAD_DIR);
                Files.createDirectories(uploadDirectory);
                String fileName = UUID.randomUUID() + extension;
                savedQrImage = uploadDirectory.resolve(fileName);
                Files.copy(qrImage.getInputStream(), savedQrImage);
                qrImagePath = "uploads/UpiQR/" + fileName;
            }

            FeesPaymentMethod feesPaymentMethod = modelMapper.map(paymentOnlineDTO, FeesPaymentMethod.class);
            feesPaymentMethod.setPaymentType(FeesPaymentMethodType.ONLINE);
            feesPaymentMethod.setFeesAdmin(feesAdmin);
            feesPaymentMethod.setQrImagePath(qrImagePath);

            feesPaymentMethod = feesPaymentMethodRepository.save(feesPaymentMethod);
            auditService.log(domain, email, "FEES_ADMIN", "FEES_PAYMENT_METHOD_CREATED",
                    "FEE_PAYMENT_METHOD", feesPaymentMethod.getId(),
                    "Online payment method created: " + feesPaymentMethod.getDisplayName());

            return "Online payment details added successfully";
        } catch (IOException | RuntimeException exception) {
            if (savedQrImage != null) {
                try {
                    Files.deleteIfExists(savedQrImage);
                } catch (IOException cleanupException) {
                    exception.addSuppressed(cleanupException);
                }
            }
            throw exception;
        }
    }


    // if student pay payment through cash so use this method
    @Transactional
    public String addCashFeesInStudentAccount(String domain, String email, FeesPaymentActionDTO paymentAction){

        FeesAdmin feesAdmin = feesAdminRepository.findByEmailAndDomain(email ,domain)
                .orElseThrow(() ->new RuntimeException( "FeesAdmin not found" ) );
        Student student = studentRepository.findByEmailAndDomainAndRollNumber(
                        paymentAction.getEmail().trim(), domain, paymentAction.getRollNumber().trim())
                .orElseThrow(() ->new RuntimeException( "Student not found" ) );

        requirePositiveAmount(paymentAction.getAmount());
        StudentFeesAccount studentFeesAccount = studentFeesAccountRepository.findByStudentId(student.getId())
                .orElseThrow(() -> new RuntimeException("Student fee account not found"));
        StudentFeesPayment studentFeesPayment = new StudentFeesPayment();
        studentFeesPayment.setStudent(student);
        studentFeesPayment.setAcademicSession(student.getAcademicSession());
        studentFeesPayment.setAmount(paymentAction.getAmount());
        studentFeesPayment.setPaymentType(FeesPaymentMethodType.CASH);
        studentFeesPayment.setStatus(FeesPaymentStatus.SUCCESS);
        studentFeesPayment.setVerifyByFeesAdmin(feesAdmin);
        studentFeesPayment.setVerifiedAt(LocalDateTime.now());
        studentFeesPayment = studentFeesPaymentRepository.save(studentFeesPayment);
        addToPaidBalance(studentFeesAccount, paymentAction.getAmount());
        auditService.log(domain, email, "FEES_ADMIN", "FEES_CASH_PAYMENT_RECORDED",
                "STUDENT_FEES_PAYMENT", studentFeesPayment.getId(),
                "Cash payment recorded for roll number " + student.getRollNumber()
                        + " (amount " + paymentAction.getAmount() + ")");

        return "Cash payment recorded successfully";
    }

    // if student pay payment online so use this method verify is payment correct and add into StrudentFeesAccount
    @Transactional
    public String verifyOnlineStudentFeesPaymentSuccess(String domain, String email, FeesPaymentReviewDTO review){

        FeesAdmin feesAdmin = feesAdminRepository.findByEmailAndDomain(email ,domain)
                .orElseThrow(() ->new RuntimeException( "FeesAdmin not found" ) );
        Student student = studentRepository.findByEmailAndDomainAndRollNumber(
                        review.getEmail().trim(), domain, review.getRollNumber().trim())
                .orElseThrow(() ->new RuntimeException( "Student not found" ) );

        StudentFeesPayment studentFeesPayment = studentFeesPaymentRepository.findByUpiTransactionIdAndStudent(
                        review.getUpiTransactionId().trim(), student)
                .orElseThrow(() ->new RuntimeException( "Upi Transaction Id not found" ) );

        requirePendingPayment(studentFeesPayment);
        studentFeesPayment.setStatus(FeesPaymentStatus.SUCCESS);
        studentFeesPayment.setVerifyByFeesAdmin(feesAdmin);
        studentFeesPayment.setVerifiedAt(LocalDateTime.now());
        studentFeesPayment = studentFeesPaymentRepository.save(studentFeesPayment);
        StudentFeesAccount studentFeesAccount = studentFeesAccountRepository.findByStudentId(student.getId())
                .orElseThrow(() -> new RuntimeException("Student fee account not found"));
        addToPaidBalance(studentFeesAccount, studentFeesPayment.getAmount());
        auditService.log(domain, email, "FEES_ADMIN", "FEES_ONLINE_PAYMENT_VERIFIED",
                "STUDENT_FEES_PAYMENT", studentFeesPayment.getId(),
                "Online payment verified for roll number " + student.getRollNumber()
                        + " (amount " + studentFeesPayment.getAmount() + ")");
        return "Online payment verified successfully";
    }

    // if student pay payment online so use this method verify is payment correct and add into StrudentFeesAccount
    @Transactional
    public String verifyOnlineStudentFeesPaymentFail(String domain, String email, FeesPaymentReviewDTO review){

        FeesAdmin feesAdmin = feesAdminRepository.findByEmailAndDomain(email ,domain)
                .orElseThrow(() ->new RuntimeException( "FeesAdmin not found" ) );
        Student student = studentRepository.findByEmailAndDomainAndRollNumber(
                        review.getEmail().trim(), domain, review.getRollNumber().trim())
                .orElseThrow(() ->new RuntimeException( "Student not found" ) );

        StudentFeesPayment studentFeesPayment = studentFeesPaymentRepository.findByUpiTransactionIdAndStudent(
                        review.getUpiTransactionId().trim(), student)
                .orElseThrow(() ->new RuntimeException( "Upi Transaction Id not found" ) );

        requirePendingPayment(studentFeesPayment);
        studentFeesPayment.setStatus(FeesPaymentStatus.REJECTED);
        studentFeesPayment.setVerifyByFeesAdmin(feesAdmin);
        studentFeesPayment.setVerifiedAt(LocalDateTime.now());
        studentFeesPayment.setRejectionReason(review.getRejectionReason());
        studentFeesPayment = studentFeesPaymentRepository.save(studentFeesPayment);
        auditService.log(domain, email, "FEES_ADMIN", "FEES_ONLINE_PAYMENT_REJECTED",
                "STUDENT_FEES_PAYMENT", studentFeesPayment.getId(),
                "Online payment rejected for roll number " + student.getRollNumber());
        return "Online payment rejected";
    }

    @Transactional(readOnly = true)
    public List<FeesPaymentMethodResponseDTO> studentPaymentMethods(String domain) {
        return feesPaymentMethodRepository.findByFeesAdminDomainAndActiveTrueOrderByIdDesc(domain)
                .stream().map(this::toPaymentMethodResponse).toList();
    }


    @Transactional(readOnly = true)
    public List<FeesPaymentMethodResponseDTO> domainAdminPaymentMethods(String domain) {
        return feesPaymentMethodRepository.findByFeesAdminDomainOrderByIdDesc(domain)
                .stream().map(this::toPaymentMethodResponse).toList();
    }

    @Transactional(readOnly = true)
    public List<FeesPaymentHistoryResponseDTO> domainAdminPaymentHistory(String domain) {
        return studentFeesPaymentRepository.findByStudent_DomainOrderByIdDesc(domain)
                .stream()
                .map(payment -> {
                    FeesPaymentHistoryResponseDTO response = new FeesPaymentHistoryResponseDTO();
                    response.setId(payment.getId());
                    response.setStudentName(payment.getStudent().getName());
                    response.setEmail(payment.getStudent().getEmail());
                    response.setRollNumber(payment.getStudent().getRollNumber());
                    response.setAcademicSession(payment.getAcademicSession());
                    response.setAmount(payment.getAmount());
                    response.setUpiTransactionId(payment.getUpiTransactionId());
                    response.setPaymentType(payment.getPaymentType().name());
                    response.setStatus(payment.getStatus().name());
                    response.setRejectionReason(payment.getRejectionReason());
                    response.setVerifiedByFeesAdminEmail(
                            payment.getVerifyByFeesAdmin() == null
                                    ? null : payment.getVerifyByFeesAdmin().getEmail());
                    response.setVerifiedAt(payment.getVerifiedAt());
                    return response;
                }).toList();
    }

    @Transactional(readOnly = true)
    public long recordedPaymentCount(String domain, String email) {
        feesAdminRepository.findByEmailAndDomain(email, domain)
                .orElseThrow(() -> new RuntimeException("Fees Admin not found"));
        return studentFeesPaymentRepository.countByStudent_Domain(domain);
    }

    @Transactional(readOnly = true)
    public List<com.demoproject.Entity.AttendenceErp.ERPActionAudit> feesAdminActivity(String domain) {
        return auditService.allForRole(domain, "FEES_ADMIN");
    }

    @Transactional
    public String setPaymentMethodActive(String domain, String email, Long methodId, boolean active) {

        FeesPaymentMethod method = feesPaymentMethodRepository
//                .findByIdAndFeesAdminEmailAndFeesAdminDomain(methodId, email, domain)
                .findByIdAndFeesAdminDomain(methodId,  domain)

                .orElseThrow(() -> new RuntimeException("Payment method not found"));
        method.setActive(active);
        feesPaymentMethodRepository.save(method);
        auditService.log(domain, email, "FEES_ADMIN",
                active ? "FEES_PAYMENT_METHOD_ACTIVATED" : "FEES_PAYMENT_METHOD_DEACTIVATED",
                "FEE_PAYMENT_METHOD", method.getId(),
                "Payment method " + method.getDisplayName()
                        + (active ? " activated" : " deactivated"));
        return active ? "Payment method activated" : "Payment method deactivated";
    }

    @Transactional(readOnly = true)
    public List<FeesPaymentQueueResponseDTO> pendingPayments(String domain, String email) {
        feesAdminRepository.findByEmailAndDomain(email, domain)
                .orElseThrow(() -> new RuntimeException("Fees Admin not found"));
        return studentFeesPaymentRepository.findByStudent_DomainAndStatusOrderByIdDesc(
                        domain, FeesPaymentStatus.PENDING)
                .stream().map(payment -> {
                    FeesPaymentQueueResponseDTO response = new FeesPaymentQueueResponseDTO();
                    response.setStudentName(payment.getStudent().getName());
                    response.setEmail(payment.getStudent().getEmail());
                    response.setRollNumber(payment.getStudent().getRollNumber());
                    response.setAcademicSession(payment.getAcademicSession());
                    response.setAmount(payment.getAmount());
                    response.setUpiTransactionId(payment.getUpiTransactionId());
                    response.setPaymentType(payment.getPaymentType().name());
                    response.setStatus(payment.getStatus().name());
                    response.setRejectionReason(payment.getRejectionReason());
                    return response;
                }).toList();
    }

    private FeesPaymentMethodResponseDTO toPaymentMethodResponse(FeesPaymentMethod method) {
        FeesPaymentMethodResponseDTO response = modelMapper.map(method, FeesPaymentMethodResponseDTO.class);
        if (method.getFeesAdmin() != null) {
            response.setFeesAdminEmail(method.getFeesAdmin().getEmail());
        }
        return response;
    }

    private void requirePositiveAmount(Double amount) {
        if (amount == null || !Double.isFinite(amount) || amount <= 0) {
            throw new RuntimeException("Payment amount must be a positive number");
        }
    }

    private void requirePendingPayment(StudentFeesPayment payment) {
        if (payment.getStatus() != FeesPaymentStatus.PENDING) {
            throw new RuntimeException("Only pending payments can be reviewed");
        }
    }

    private void addToPaidBalance(StudentFeesAccount account, Double amount) {
        requirePositiveAmount(amount);
        double totalPaid = account.getTotalPaid() == null ? 0.0 : account.getTotalPaid();
        account.setTotalPaid(totalPaid + amount);
        studentFeesAccountRepository.save(account);
    }


}
