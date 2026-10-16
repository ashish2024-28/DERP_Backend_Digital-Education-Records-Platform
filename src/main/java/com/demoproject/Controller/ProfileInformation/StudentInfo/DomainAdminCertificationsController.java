package com.demoproject.Controller.ProfileInformation.StudentInfo;

import com.demoproject.Service.ProfileInformation.StudentInfo.CertificationsService;
import lombok.RequiredArgsConstructor;
import org.springframework.core.io.Resource;
import org.springframework.http.ContentDisposition;
import org.springframework.http.HttpHeaders;
import org.springframework.http.MediaType;
import org.springframework.http.MediaTypeFactory;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/{domain}/domain-admin/student/certifications")
@PreAuthorize("hasRole('DOMAIN_ADMIN') and #domain.equalsIgnoreCase(authentication.principal.domain)")
@RequiredArgsConstructor
public class DomainAdminCertificationsController {

    private final CertificationsService certificationsService;

    @GetMapping("/{id}/file")
    public ResponseEntity<Resource> download(
            @PathVariable String domain,
            @PathVariable Long id) {
        Resource resource = certificationsService.getCertificateFileForDomainAdmin(id, domain);
        MediaType contentType = MediaTypeFactory.getMediaType(resource)
                .orElse(MediaType.APPLICATION_OCTET_STREAM);
        return ResponseEntity.ok()
                .contentType(contentType)
                .header(HttpHeaders.CONTENT_DISPOSITION,
                        ContentDisposition.attachment().filename("certificate-" + id).build().toString())
                .body(resource);
    }
}
