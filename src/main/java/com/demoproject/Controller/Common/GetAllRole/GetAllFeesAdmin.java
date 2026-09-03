package com.demoproject.Controller.Common.GetAllRole;

import com.demoproject.DTO.FeesAdminDto.FeesAdminResponseDTO;
import com.demoproject.Service.FeesAdminService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.GrantedAuthority;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.util.List;

@RestController
@RequestMapping("/{domain}/{role}")
@PreAuthorize("""
        hasAnyRole("ADMIN", "DOMAIN_ADMIN", "FEES_ADMIN") and
        #domain.equalsIgnoreCase(authentication.principal.domain)
        """)
public class GetAllFeesAdmin {

    @Autowired
    private FeesAdminService feesAdminService;

    @GetMapping("/allFeesAdmin")
    public List<FeesAdminResponseDTO> getAllFeesAdmin(
            @PathVariable String domain,
            Authentication authentication
    ) {

        List<FeesAdminResponseDTO> feesAdmins =
                feesAdminService.getAllFeesAdmin(domain);

        boolean isDomainAdmin = authentication
                .getAuthorities()
                .stream()
                .map(GrantedAuthority::getAuthority)
                .anyMatch("ROLE_DOMAIN_ADMIN"::equals);

        // Password is visible only to DOMAIN_ADMIN.
        if (!isDomainAdmin) {
            feesAdmins.forEach(admin -> admin.setPassword(null));
        }

        return feesAdmins;
    }
}
