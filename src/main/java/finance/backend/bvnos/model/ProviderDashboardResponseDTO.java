package finance.backend.bvnos.model;

import lombok.Data;

import java.math.BigDecimal;

@Data
public class ProviderDashboardResponseDTO {
    private String id;
    private String name;
    private Boolean active;
    private BigDecimal totalInPayments;
}
