package com.demoproject.DTO.BulkUpload;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@NoArgsConstructor
@AllArgsConstructor
public class BulkUploadDuplicateDTO {

    private String fileName;

    private int rowNumber;

    private String email;

    private String reason;
}
