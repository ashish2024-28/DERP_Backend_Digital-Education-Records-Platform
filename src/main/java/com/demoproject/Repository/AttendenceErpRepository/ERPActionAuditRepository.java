package com.demoproject.Repository.AttendenceErpRepository;

import com.demoproject.Entity.AttendenceErp.ERPActionAudit;

import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;

public interface ERPActionAuditRepository
        extends JpaRepository<ERPActionAudit, Long> {

    List<ERPActionAudit> findByDomainIgnoreCaseOrderByCreatedAtDesc(
            String domain
    );

    List<ERPActionAudit> findByDomainIgnoreCaseAndRoleIgnoreCaseOrderByCreatedAtDesc(
            String domain,
            String role
    );
}

//
//
//For example:
//
//DomainAdmin deleted attendance
//Faculty marked attendance
//DomainAdmin corrected attendance
//SubAdmin changed something