package com.demoproject.DTO.BulkUpload;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.util.ArrayList;
import java.util.List;

@Data
@NoArgsConstructor
public class BulkUploadResultDTO {

    private int totalRows;
    private int savedRows;
    private int duplicateRows;
    private int invalidRows;

    private List<BulkUploadDuplicateDTO> duplicates =
            new ArrayList<>();

    private List<BulkUploadDuplicateDTO> invalidRecords =
            new ArrayList<>();

    public void addDuplicate(
            String fileName,
            int rowNumber,
            String email,
            String reason) {

        duplicateRows++;

        duplicates.add(
                new BulkUploadDuplicateDTO(
                        fileName,
                        rowNumber,
                        email,
                        reason
                )
        );
    }

    public void addInvalid(
            String fileName,
            int rowNumber,
            String email,
            String reason) {

        invalidRows++;

        invalidRecords.add(
                new BulkUploadDuplicateDTO(
                        fileName,
                        rowNumber,
                        email,
                        reason
                )
        );
    }
}

