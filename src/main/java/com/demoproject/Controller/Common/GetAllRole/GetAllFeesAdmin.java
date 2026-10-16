package com.demoproject.Controller.Common.GetAllRole;

import com.demoproject.DTO.FacultyDTO.FacultyResponseDTO;
import com.demoproject.DTO.FeesAdminDto.FeesAdminResponseDTO;
import com.demoproject.DTO.SubAdminDTO.SubAdminResponseDTO;
import com.demoproject.Service.FeesAdminService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.GrantedAuthority;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.Set;
import java.util.stream.Collectors;

@RestController
@RequestMapping("/{domain}/{role}")
@PreAuthorize("""
        hasAnyRole("ADMIN", "DOMAIN_ADMIN", "FEES_ADMIN") and
        #domain.equalsIgnoreCase(authentication.principal.domain)
        """)
public class GetAllFeesAdmin {

    @Autowired
    private FeesAdminService feesAdminService;


    @GetMapping("/feesAdmin")
    public FeesAdminResponseDTO getAllFeesAdmin(@PathVariable String domain , @RequestParam String email  , Authentication authentication ) {

        Set<String> roles = authentication.getAuthorities().stream()
                .map(authority -> authority.getAuthority())
                .collect(Collectors.toSet());
        return feesAdminService.getFeesWithProfile(domain, email, roles );
    }
    @GetMapping("/allFeesAdmin")
    public List<FeesAdminResponseDTO> getAllFeesAdmin(
            @PathVariable String domain,
            Authentication authentication
    ) {

        List<FeesAdminResponseDTO> feesAdmins =
                feesAdminService.getAllFeesAdmin(domain);

        return feesAdmins;
    }
}
