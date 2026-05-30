package finance.backend.bvnos.model;

import lombok.Data;
import org.springframework.data.annotation.Id;
import org.springframework.data.mongodb.core.mapping.Document;

import java.time.LocalDateTime;

@Data
@Document(collection = "reconciliation_process")
public class ReconciliationProcess {
    @Id
    private String id;

    private String name;

    private FileInformation fileA;
    private FileInformation fileB;

    private ReconciliationProcessStatus status;
    // PROCESSING | COMPLETED | FAILED

    private Integer totalRecords;
    private Integer matchedRecords;
    private Integer mismatchedRecords;

    private LocalDateTime createdAt;
}
