package com.demoproject.Controller.Common.GetAllRole;


import com.demoproject.DTO.StudentDTO.StudentResponseWithProfileDTO;
import com.demoproject.DTO.SubAdminDTO.SubAdminResponseDTO;
import com.demoproject.Service.SubAdminService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.security.core.Authentication;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.Set;
import java.util.stream.Collectors;

@RestController
@RequestMapping("/{domain}/{role}")
@PreAuthorize("""
        hasAnyRole("ADMIN","DOMAIN_ADMIN","SUB_ADMIN") and
        #domain.equalsIgnoreCase(authentication.principal.domain)
        """)
public class GetAllSubAdmin {
    @Autowired
    private SubAdminService subAdminService;

    @GetMapping("/subAdmin")
    public SubAdminResponseDTO getAllStudents(@PathVariable String domain , @RequestParam String email  , Authentication authentication ) {

        Set<String> roles = authentication.getAuthorities().stream()
                .map(authority -> authority.getAuthority())
                .collect(Collectors.toSet());
        return subAdminService.getSubAdminWithProfile(domain, email, roles );
    }

    // READ ALL subadmin by domain
    @GetMapping("/allSubAdmin")
    public List<SubAdminResponseDTO> getAllSubAdmin(
            @PathVariable String domain,
            Authentication authentication ) {

        String role = authentication.getAuthorities().toString();

        List<SubAdminResponseDTO> subAdmin = subAdminService.getAllSubAdmin(domain);

        return subAdmin;
    }


}
