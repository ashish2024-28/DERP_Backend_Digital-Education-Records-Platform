package com.demoproject.Controller;
    
import java.util.List;

import com.demoproject.DTO.AdminActionAuditResponseDTO;
import com.demoproject.DTO.AdminUniversityDeleteOtpRequestDTO;
import com.demoproject.DTO.AdminUniversityDeleteRequestDTO;
import com.demoproject.DTO.AdminUniversityResponseDTO;
import com.demoproject.DTO.DomainAdminDTO.DomainAdminResponseDTO;
import com.demoproject.Entity.PlatformAdminActionAudit;
import com.demoproject.Repository.PlatformAdminActionAuditRepository;
import com.demoproject.Service.PlatformAdminDeletionService;
import com.demoproject.Service.PlatformAdminOtpService;
import jakarta.validation.Valid;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.security.core.Authentication;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import com.demoproject.Entity.University;
import com.demoproject.Service.DomainAdminService;
import com.demoproject.Service.UniversityService;


@RestController
@RequestMapping("/admin")
@PreAuthorize("hasRole('ADMIN')")
public class AdminController {
    
    @Autowired
    private UniversityService universityService;
    @Autowired
    private DomainAdminService domainAdminService;
    @Autowired
    private PlatformAdminOtpService platformAdminOtpService;
    @Autowired
    private PlatformAdminDeletionService platformAdminDeletionService;
    @Autowired
    private PlatformAdminActionAuditRepository auditRepository;
    
    @GetMapping
    public String test(){
        return "ADMIN Access OK";
    }
    
    /* ================= DOMAIN ADMIN ================= */

    @GetMapping("/all_domain_admins")
    public List<DomainAdminResponseDTO> getAllDomainAdmins() {
        return domainAdminService.getAllDomainAdmin().stream().map(domainAdmin -> {
            DomainAdminResponseDTO response = new DomainAdminResponseDTO();
            response.setName(domainAdmin.getName());
            response.setDomain(domainAdmin.getDomain());
            response.setMobileNumber(domainAdmin.getMobileNumber());
            response.setEmail(domainAdmin.getEmail());
            response.setCreatedDateTime(domainAdmin.getCreatedDateTime());
            response.setLastUpdateDateTime(domainAdmin.getLastUpdateDateTime());
            response.setLastLoginDateTime(domainAdmin.getLastLoginDateTime());
            response.setProfilePic(domainAdmin.getProfilePic());
            return response;
        }).toList();
    }
    
    /* ================= UNIVERSITY ================= */

    // GET ALL
    @GetMapping("/all_universities")
    public List<AdminUniversityResponseDTO> getAllUniversities() {
        return universityService.getAllForPlatformAdmin();
    }
    
    // GET BY ID
    @GetMapping("/universitie_id")
    public AdminUniversityResponseDTO getUniversityById(@RequestParam Long id) {
        return universityService.getForPlatformAdminById(id);
    }

    // This will match /hu, /dtu, /aku etc.
    @GetMapping("/universitie_domain")
    public AdminUniversityResponseDTO getByDomain(@RequestParam String domain) {
        return universityService.getForPlatformAdminByDomain(domain);
    }

    // UPDATE id and domain
    @PutMapping("/universitie_update/id_domain")
    public AdminUniversityResponseDTO update(@RequestParam String domain, @RequestParam Long id, @RequestBody University univ){
        universityService.updateUniversity(domain,id,univ);
        return universityService.getForPlatformAdminById(id);
    }

    @PostMapping("/universities/{id}/delete-otp")
    public com.demoproject.DTO.ApiResponse<String> sendUniversityDeleteOtp(
                @PathVariable Long id,
                @Valid @RequestBody AdminUniversityDeleteOtpRequestDTO request,
                Authentication authentication) {
            platformAdminOtpService.sendDeleteOtp(
                    id, request.getDomain(), authentication.getName(), request.getReason());
            return new com.demoproject.DTO.ApiResponse<>(true, "OTP sent to the platform administrator's registered email", null);
    }

    @GetMapping("/audit_logs")
    public List<AdminActionAuditResponseDTO> getAuditLogs() {
        return auditRepository.findTop100ByOrderByCreatedAtDesc().stream().map(this::toAuditResponse).toList();
    }

    private AdminActionAuditResponseDTO toAuditResponse(PlatformAdminActionAudit audit) {
        AdminActionAuditResponseDTO response = new AdminActionAuditResponseDTO();
        response.setAdminEmail(audit.getAdminEmail());
        response.setEventType(audit.getEventType());
        response.setUniversityDomain(audit.getUniversityDomain());
        response.setReason(audit.getReason());
        response.setCreatedAt(audit.getCreatedAt());
        return response;
    }

    // Deletion cascades to all university-owned accounts and records.
    @DeleteMapping("/universitie_delete/id_domain")
    public com.demoproject.DTO.ApiResponse<String> delete(
            @RequestParam Long id,
            @Valid @RequestBody AdminUniversityDeleteRequestDTO request,
            Authentication authentication) {
        String result = platformAdminDeletionService.deleteUniversity(
                id, request.getDomain(), request.getConfirmation(), authentication.getName(),
                request.getReason(), request.getOtp());
        return new com.demoproject.DTO.ApiResponse<>(true, result, null);
    }

}
