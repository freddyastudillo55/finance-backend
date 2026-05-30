package finance.backend.bvnos.model;

import lombok.Data;

import java.math.BigDecimal;

@Data
public class ReconciliationSource {

    private String system;

    private BigDecimal originalAmount;

    private BigDecimal adjustedAmount;
}
