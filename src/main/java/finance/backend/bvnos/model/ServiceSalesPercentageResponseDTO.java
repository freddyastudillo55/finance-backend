package finance.backend.bvnos.model;

import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

@Getter
@Setter
@AllArgsConstructor
@NoArgsConstructor
public class ServiceSalesPercentageResponseDTO {
    private String service;
    private Long totalSales;
    private Double percentage;
}
