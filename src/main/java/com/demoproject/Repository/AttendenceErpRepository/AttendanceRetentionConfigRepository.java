package com.demoproject.Repository.AttendenceErpRepository;

import com.demoproject.Entity.AttendenceErp.AttendanceRetentionConfig;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.Optional;

public interface AttendanceRetentionConfigRepository
        extends JpaRepository<AttendanceRetentionConfig, Long> {

    Optional<AttendanceRetentionConfig>
    findByDomainIgnoreCase(String domain);
}