package finance.backend.bvnos.model;

import lombok.Data;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.time.LocalDateTime;

@Data
public class ProviderPaymentResponseDTO {

    private String id;

    private String providerId;
    private String providerName;

    private BigDecimal amount;

    private LocalDate paymentDate;
    private String status;

    private String description;

    private LocalDateTime createdAt;
    private LocalDateTime updatedAt;

    private Boolean active;
}
