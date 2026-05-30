package finance.backend.bvnos.model;

import lombok.Data;

import java.time.LocalDateTime;

@Data
public class ReconciliationProcessResponseDTO {
    private String id;
    private String name;
    private FileInformation fileA;
    private FileInformation fileB;
    private String status;
    private Integer totalRecords;
    private Integer matchedRecords;
    private Integer mismatchedRecords;
    private LocalDateTime createdAt;
}
