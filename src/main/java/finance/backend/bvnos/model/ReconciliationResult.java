package finance.backend.bvnos.model;

import lombok.Data;
import org.springframework.data.annotation.Id;
import org.springframework.data.mongodb.core.mapping.Document;

import java.math.BigDecimal;
import java.time.LocalDateTime;

@Data
@Document(collection = "reconciliation_results")
public class ReconciliationResult {
    @Id
    private String id;

    private String processId;

    private String customerId;

    private String service;

    private ReconciliationSource sourceA;
    private ReconciliationSource sourceB;

    private BigDecimal finalAmount;

    private ReconciliationStatus status;
    // RECONCILED | MISMATCH

    private BigDecimal difference;

    private Adjustment adjustment;

    private LocalDateTime createdAt;

    private Boolean active;

    private LocalDateTime deletedAt;

    private LocalDateTime reconciledAt;
}
