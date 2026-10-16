package com.demoproject.Repository;

import com.demoproject.Entity.PlatformAdminActionAudit;
import java.util.List;
import org.springframework.data.jpa.repository.JpaRepository;

public interface PlatformAdminActionAuditRepository extends JpaRepository<PlatformAdminActionAudit, Long> {

    List<PlatformAdminActionAudit> findTop100ByOrderByCreatedAtDesc();
}
