package finance.backend.bvnos.model;

import lombok.Data;

import java.math.BigDecimal;
import java.time.LocalDateTime;

@Data
public class UpdateReconciliationResultRequestDTO {
    private BigDecimal finalAmount;
    private LocalDateTime reconciledAt;
}
