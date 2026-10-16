package com.demoproject.Controller.ProfileInformation.StudentInfo;

import java.io.IOException;
import java.util.List;

import com.demoproject.DTO.CertificationResponseDTO;
import org.springframework.core.io.Resource;
import org.springframework.http.ContentDisposition;
import org.springframework.http.HttpHeaders;
import org.springframework.http.MediaType;
import org.springframework.http.MediaTypeFactory;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.Authentication;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;

import lombok.RequiredArgsConstructor;

import com.demoproject.Service.ProfileInformation.StudentInfo.CertificationsService;

@RestController
@RequestMapping("/{domain}/student/certifications")
@PreAuthorize("hasRole('STUDENT') and #domain.equalsIgnoreCase(authentication.principal.domain)")

@RequiredArgsConstructor
public class CertificationsController {

    private final CertificationsService certificationsService;

    @GetMapping
    public String greet(){
        return "welcom to certifications....";
    }

    // ADD Certification
    @PostMapping("/add")
    public CertificationResponseDTO addCertification(
            @PathVariable String domain,
            Authentication authentication,
            @RequestParam String title,
            @RequestParam String description,
            @RequestParam MultipartFile file) throws IOException {

        String email = authentication.getName();

        return certificationsService.addCertification(
                email, domain, title, description, file);
    }

    // GET My Certifications
    @GetMapping("/my")
    public List<CertificationResponseDTO> getMyCertifications(
            @PathVariable String domain,
            Authentication authentication) {

        String email = authentication.getName();

        return certificationsService.getMyCertifications(email, domain);
    }

    @GetMapping("/{id}/file")
    public ResponseEntity<Resource> download(
            @PathVariable String domain,
            @PathVariable Long id,
            Authentication authentication) {
        Resource resource = certificationsService.getCertificateFile(
                id, authentication.getName(), domain);
        MediaType contentType = MediaTypeFactory.getMediaType(resource)
                .orElse(MediaType.APPLICATION_OCTET_STREAM);
        return ResponseEntity.ok()
                .contentType(contentType)
                .header(HttpHeaders.CONTENT_DISPOSITION,
                        ContentDisposition.attachment().filename("certificate-" + id).build().toString())
                .body(resource);
    }

    // DELETE
    @DeleteMapping("/{id}")
    public String delete(
            @PathVariable String domain,
            @PathVariable Long id,
            Authentication authentication) {
        certificationsService.deleteCertification(id, authentication.getName(), domain);
        return "Deleted Successfully";
    }

    // SEARCH
    @GetMapping("/search")
    public List<CertificationResponseDTO> search(
            @PathVariable String domain,
            @RequestParam String title,
            Authentication authentication) {
        return certificationsService.searchByTitle(title, authentication.getName(), domain);
    }

}
