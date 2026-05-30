package finance.backend.bvnos.model;

import lombok.Data;

import java.math.BigDecimal;
import java.time.LocalDate;

@Data
public class SalesDetailsResponseDTO {
    private LocalDate date;
    private BigDecimal totalSales;
    private Long totalTransactions;
}
