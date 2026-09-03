package com.demoproject.Service.AttendenceErpService;

import com.demoproject.Entity.AttendenceErp.ERPActionAudit;
import com.demoproject.Repository.AttendenceErpRepository.ERPActionAuditRepository;

import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;
import java.util.List;

@Service
public class ERPActionAuditService {

 private final ERPActionAuditRepository repository;

 public ERPActionAuditService(
         ERPActionAuditRepository repository
 ) {
  this.repository = repository;
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

  ERPActionAudit audit =
          new ERPActionAudit();

  audit.setDomain(domain);
  audit.setActorEmail(email);
  audit.setRole(role);
  audit.setAction(action);
  audit.setTargetType(targetType);
  audit.setTargetId(targetId);
  audit.setDetails(details);
  audit.setCreatedAt(LocalDateTime.now());

  repository.save(audit);
 }

 @Transactional(readOnly = true)
 public List<ERPActionAudit> all(
         String domain
 ) {

  return repository
          .findByDomainOrderByCreatedAtDesc(domain);
 }
}