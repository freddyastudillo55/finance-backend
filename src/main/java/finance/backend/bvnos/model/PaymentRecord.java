package finance.backend.bvnos.model;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.math.BigDecimal;
import java.time.LocalDate;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class PaymentRecord {
    private String customerId;
    private String service;
    private BigDecimal amount;
    private LocalDate paymentDate;
    private String source;
}
