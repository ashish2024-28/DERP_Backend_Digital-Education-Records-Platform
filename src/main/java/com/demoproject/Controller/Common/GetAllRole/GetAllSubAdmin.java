package com.demoproject.Controller.Common.GetAllRole;


import com.demoproject.DTO.SubAdminDTO.SubAdminResponseDTO;
import com.demoproject.Service.SubAdminService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.security.core.Authentication;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.util.List;

@RestController
@RequestMapping("/{domain}/{role}")
@PreAuthorize("""
        hasAnyRole("ADMIN","DOMAIN_ADMIN","SUB_ADMIN") and
        #domain.equalsIgnoreCase(authentication.principal.domain)
        """)
public class GetAllSubAdmin {
    @Autowired
    private SubAdminService subAdminService;


    // READ ALL subadmin by domain
    @GetMapping("/allSubAdmin")
    public List<SubAdminResponseDTO> getAllSubAdmin(
            @PathVariable String domain,
            Authentication authentication ) {

        String role = authentication.getAuthorities().toString();

        List<SubAdminResponseDTO> subAdmin = subAdminService.getAllSubAdmin(domain);

        if(!role.contains("DOMAIN_ADMIN")){
            subAdmin.forEach(s -> s.setPassword(null));
        }

        return subAdmin;
    }


}
