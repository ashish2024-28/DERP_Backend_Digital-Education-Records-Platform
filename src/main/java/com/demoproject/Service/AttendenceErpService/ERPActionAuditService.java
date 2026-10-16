package com.demoproject.Service.AttendenceErpService;

import com.demoproject.Entity.AttendenceErp.ERPActionAudit;
import com.demoproject.Repository.AttendenceErpRepository.ERPActionAuditRepository;

import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;
import java.util.Locale;
import java.util.List;
import java.util.Objects;

@Service
public class ERPActionAuditService {

 private final ERPActionAuditRepository repository;

 public ERPActionAuditService(
         ERPActionAuditRepository repository
 ) {
  this.repository = Objects.requireNonNull(repository, "repository is required");
 }

 @Transactional
 public void log(
         String domain,
         String email,
         String role,
         String action,
         String targetType,
         Long targetId,
         String details
 ) {
  String normalizedDomain = normalizeRequired(domain, "domain");
  String normalizedEmail = normalizeRequired(email, "actor email");
  String normalizedRole = normalizeRequired(role, "actor role");
  String normalizedAction = normalizeRequired(action, "action");

  ERPActionAudit audit =
          new ERPActionAudit();

  audit.setDomain(normalizedDomain);
  audit.setActorEmail(normalizedEmail);
  audit.setRole(normalizedRole);
  audit.setAction(normalizedAction);
  audit.setTargetType(targetType == null ? null : targetType.trim());
  audit.setTargetId(targetId);
  audit.setDetails(details == null ? null : details.trim());
  audit.setCreatedAt(LocalDateTime.now());

  repository.save(audit);
 }

 @Transactional(readOnly = true)
 public List<ERPActionAudit> all(
         String domain
 ) {

  return repository
          .findByDomainIgnoreCaseOrderByCreatedAtDesc(
                  normalizeRequired(domain, "domain")
          );
 }

 @Transactional(readOnly = true)
 public List<ERPActionAudit> allForRole(String domain, String role) {
  return repository.findByDomainIgnoreCaseAndRoleIgnoreCaseOrderByCreatedAtDesc(
          normalizeRequired(domain, "domain"),
          normalizeRequired(role, "role")
  );
 }

 private String normalizeRequired(String value, String fieldName) {
  if (value == null || value.isBlank()) {
   throw new IllegalArgumentException(fieldName + " is required");
  }
  return value.trim().toLowerCase(Locale.ROOT);
 }
}