package finance.backend.bvnos.model;

import lombok.Data;

import java.math.BigDecimal;
import java.time.LocalDate;

@Data
public class ProviderPaymentRequestDTO {
    private String providerId;
    private String providerName;

    private BigDecimal amount;

    private LocalDate paymentDate;
    private String status;

    private String description;
}
