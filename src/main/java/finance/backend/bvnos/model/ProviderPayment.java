package finance.backend.bvnos.model;

import lombok.Data;
import org.springframework.data.annotation.Id;
import org.springframework.data.mongodb.core.mapping.Document;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.time.LocalDateTime;

@Data
@Document(collection = "provider_payments")
public class ProviderPayment {
    @Id
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
