package finance.backend.bvnos.model;

import lombok.Data;

import java.math.BigDecimal;
import java.time.LocalDateTime;

@Data
public class ReconciliationResultResponseDTO {
    private String id;
    private String processId;
    private String customerId;
    private String service;
    private ReconciliationSource sourceA;
    private ReconciliationSource sourceB;
    private BigDecimal finalAmount;
    private String status;
    private BigDecimal difference;
    private Adjustment adjustment;
    private LocalDateTime createdAt;
    private Boolean active;
    private LocalDateTime reconciledAt;
}
