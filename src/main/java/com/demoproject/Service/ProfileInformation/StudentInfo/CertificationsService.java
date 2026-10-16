package com.demoproject.Service.ProfileInformation.StudentInfo;

import java.io.IOException;
import java.nio.file.*;
import java.util.List;
import java.util.UUID;

import com.demoproject.DTO.CertificationResponseDTO;
import org.springframework.stereotype.Service;
import org.springframework.web.multipart.MultipartFile;
import org.springframework.core.io.FileSystemResource;
import org.springframework.core.io.Resource;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.http.HttpStatus;
import org.springframework.web.server.ResponseStatusException;

import lombok.RequiredArgsConstructor;

import com.demoproject.Entity.Student;
import com.demoproject.Entity.ProfileInformation.StudentInfo.Certifications;
import com.demoproject.Repository.StudentRepository;
import com.demoproject.Repository.ProfileInformation.StudentProfile.CertificationsRepository;

@Service
@RequiredArgsConstructor
public class CertificationsService {

    private final CertificationsRepository certificationsRepository;
    private final StudentRepository studentRepository;

    private static final String UPLOAD_DIR = "private_uploads/certificates/";
    private static final String LEGACY_UPLOAD_DIR = "uploads/certificates/";

    // ADD Certification (Logged-in user)
    @Transactional
    public CertificationResponseDTO addCertification(
            String email,
            String domain,
            String title,
            String description,
            MultipartFile file) throws IOException {

        Student student = studentRepository.findByEmailAndDomain(email, domain)
                .orElseThrow(() -> new RuntimeException("Student not found"));

        if (title == null || title.isBlank() || description == null || description.isBlank()) {
            throw new ResponseStatusException(HttpStatus.BAD_REQUEST, "Title and description are required");
        }
        if (file == null || file.isEmpty()) {
            throw new ResponseStatusException(HttpStatus.BAD_REQUEST, "A certificate file is required");
        }

        Path uploadDirectory = Paths.get(UPLOAD_DIR);
        Files.createDirectories(uploadDirectory);

        String originalName = Paths.get(file.getOriginalFilename() == null
                ? "certificate" : file.getOriginalFilename()).getFileName().toString();
        getAllowedExtension(originalName);
        String fileName = UUID.randomUUID() + "_" + originalName;
        Path filePath = uploadDirectory.resolve(fileName);

        file.transferTo(filePath);

        Certifications cert = new Certifications();
        cert.setTitle(title.trim());
        cert.setDescription(description.trim());
        cert.setCertificateFilePath(UPLOAD_DIR + fileName);
        cert.setStudent(student);

        return toResponse(certificationsRepository.save(cert), domain);
    }

    // GET My Certifications
    @Transactional(readOnly = true)
    public List<CertificationResponseDTO> getMyCertifications(String email, String domain) {
        Student student = studentRepository.findByEmailAndDomain(email, domain)
                .orElseThrow(() -> new RuntimeException("Student not found"));

        return certificationsRepository.findByStudentIdOrderByIdDesc(student.getId())
                .stream().map(certification -> toResponse(certification, domain)).toList();
    }

    // DELETE
    @Transactional
    public void deleteCertification(Long id, String email, String domain) {
        Student student = studentRepository.findByEmailAndDomain(email, domain)
                .orElseThrow(() -> new RuntimeException("Student not found"));

        Certifications certification = certificationsRepository.findByIdAndStudentId(id, student.getId())
                .orElseThrow(() -> new ResponseStatusException(HttpStatus.NOT_FOUND, "Certification not found"));

        Path certificateFile = resolveCertificatePath(certification.getCertificateFilePath());

        certificationsRepository.delete(certification);
        try {
            Files.deleteIfExists(certificateFile);
        } catch (IOException exception) {
            throw new IllegalStateException("Certification record was not deleted because its file could not be removed", exception);
        }
    }

    // SEARCH
    @Transactional(readOnly = true)
    public List<CertificationResponseDTO> searchByTitle(String title, String email, String domain) {
        Student student = studentRepository.findByEmailAndDomain(email, domain)
                .orElseThrow(() -> new RuntimeException("Student not found"));
        String query = title == null ? "" : title.trim();
        return (query.isEmpty()
                ? certificationsRepository.findByStudentIdOrderByIdDesc(student.getId())
                : certificationsRepository.findByStudentIdAndTitleContainingIgnoreCaseOrderByIdDesc(
                        student.getId(), query))
                .stream().map(certification -> toResponse(certification, domain)).toList();
    }

    @Transactional(readOnly = true)
    public Resource getCertificateFile(Long id, String email, String domain) {
        Student student = studentRepository.findByEmailAndDomain(email, domain)
                .orElseThrow(() -> new RuntimeException("Student not found"));
        Certifications certification = certificationsRepository.findByIdAndStudentId(id, student.getId())
                .orElseThrow(() -> new ResponseStatusException(HttpStatus.NOT_FOUND, "Certification not found"));
        return getCertificateResource(certification);
    }

    @Transactional(readOnly = true)
    public Resource getCertificateFileForDomainAdmin(Long id, String domain) {
        Certifications certification = certificationsRepository.findById(id)
                .filter(item -> item.getStudent() != null
                        && item.getStudent().getDomain() != null
                        && item.getStudent().getDomain().equalsIgnoreCase(domain))
                .orElseThrow(() -> new ResponseStatusException(HttpStatus.NOT_FOUND, "Certification not found"));
        return getCertificateResource(certification);
    }

    private Resource getCertificateResource(Certifications certification) {
        Path certificateFile = resolveCertificatePath(certification.getCertificateFilePath());
        if (!Files.isRegularFile(certificateFile)) {
            throw new ResponseStatusException(HttpStatus.NOT_FOUND, "Certificate file not found");
        }
        return new FileSystemResource(certificateFile);
    }

    private CertificationResponseDTO toResponse(Certifications certification, String domain) {
        CertificationResponseDTO response = new CertificationResponseDTO();
        response.setId(certification.getId());
        response.setTitle(certification.getTitle());
        response.setDescription(certification.getDescription());
        response.setDownloadUrl("/" + domain + "/student/certifications/" + certification.getId() + "/file");
        return response;
    }

    private String getAllowedExtension(String fileName) {
        String extension = fileName.substring(fileName.lastIndexOf('.') + 1).toLowerCase();
        if (!List.of("pdf", "png", "jpg", "jpeg").contains(extension)) {
            throw new ResponseStatusException(
                    HttpStatus.BAD_REQUEST, "Certificate files must be PDF, PNG, JPG, or JPEG");
        }
        return extension;
    }

    private Path resolveCertificatePath(String storedPath) {
        String normalizedStoredPath = storedPath.replace('\\', '/');
        String allowedDirectory;
        if (normalizedStoredPath.startsWith(UPLOAD_DIR)) {
            allowedDirectory = UPLOAD_DIR;
        } else if (normalizedStoredPath.startsWith(LEGACY_UPLOAD_DIR)) {
            allowedDirectory = LEGACY_UPLOAD_DIR;
        } else {
            throw new ResponseStatusException(
                    HttpStatus.INTERNAL_SERVER_ERROR, "Certificate file path is invalid");
        }
        Path allowedRoot = Paths.get(allowedDirectory).toAbsolutePath().normalize();
        Path filePath = Paths.get(normalizedStoredPath).toAbsolutePath().normalize();
        if (!filePath.startsWith(allowedRoot)) {
            throw new ResponseStatusException(
                    HttpStatus.INTERNAL_SERVER_ERROR, "Certificate file path is invalid");
        }
        return filePath;
    }
}
