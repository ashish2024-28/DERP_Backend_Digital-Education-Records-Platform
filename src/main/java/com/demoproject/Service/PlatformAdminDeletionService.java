package com.demoproject.Service;

import com.demoproject.Entity.PlatformAdminActionAudit;
import com.demoproject.Entity.PlatformAdminDeleteChallenge;
import com.demoproject.Entity.University;
import com.demoproject.Repository.PlatformAdminActionAuditRepository;
import com.demoproject.Repository.PlatformAdminDeleteChallengeRepository;
import com.demoproject.Repository.UniversityRepo;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.http.HttpStatus;
import org.springframework.web.server.ResponseStatusException;

@Service
@RequiredArgsConstructor
public class PlatformAdminDeletionService {

    private final UniversityRepo universityRepo;
    private final PlatformAdminActionAuditRepository auditRepository;
    private final PlatformAdminDeleteChallengeRepository challengeRepository;
    private final PlatformAdminOtpService otpService;

    @Transactional
    public String deleteUniversity(Long id, String domain, String confirmation, String adminEmail, String reason, String otp) {
        if (!domain.equals(confirmation)) {
            throw new ResponseStatusException(
                    HttpStatus.BAD_REQUEST, "Type the university domain exactly to confirm");
        }
        University university = universityRepo.findByDomainAndId(domain, id)
                .orElseThrow(() -> new ResponseStatusException(
                        HttpStatus.NOT_FOUND, "University not found for the supplied domain and ID"));

        PlatformAdminDeleteChallenge challenge = challengeRepository
                .findTopByAdminEmailAndUniversityIdAndUniversityDomainAndReasonAndConsumedAtIsNullOrderByCreatedAtDesc(
                        adminEmail, id, domain, reason.trim())
                .orElseThrow(() -> new ResponseStatusException(
                        HttpStatus.BAD_REQUEST, "Request a fresh OTP for this university and deletion reason"));
        if (challenge.getExpiresAt().isBefore(java.time.LocalDateTime.now())) {
            throw new ResponseStatusException(HttpStatus.BAD_REQUEST, "Deletion OTP has expired");
        }
        otpService.verifyDeleteOtp(adminEmail, otp);
        challenge.setConsumedAt(java.time.LocalDateTime.now());
        challengeRepository.save(challenge);

        PlatformAdminActionAudit audit = new PlatformAdminActionAudit();
        audit.setAdminEmail(adminEmail);
        audit.setEventType("UNIVERSITY_DELETED");
        audit.setUniversityDomain(university.getDomain());
        audit.setReason(reason.trim());
        auditRepository.save(audit);

        universityRepo.delete(university);
        return "University and its associated records were deleted";
    }
}
