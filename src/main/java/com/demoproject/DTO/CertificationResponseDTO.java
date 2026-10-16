package com.demoproject.DTO;

import lombok.Getter;
import lombok.Setter;

@Getter
@Setter
public class CertificationResponseDTO {

    private Long id;
    private String title;
    private String description;
    private String downloadUrl;
}
