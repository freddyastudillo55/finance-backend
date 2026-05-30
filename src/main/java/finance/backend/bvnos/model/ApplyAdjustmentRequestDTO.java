package finance.backend.bvnos.model;

import lombok.Data;

import java.math.BigDecimal;

@Data
public class ApplyAdjustmentRequestDTO {
    private BigDecimal aaxAdjustedAmount;
    private BigDecimal vestaAdjustedAmount;
    private String adjustedBy;
    private String reason;
}
