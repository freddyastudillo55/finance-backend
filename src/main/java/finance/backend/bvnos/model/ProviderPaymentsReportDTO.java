package finance.backend.bvnos.model;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@AllArgsConstructor
@NoArgsConstructor
public class ProviderPaymentsReportDTO {
    private String providerName;
    private Double totalAmount;
}
