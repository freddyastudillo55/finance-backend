package finance.backend.bvnos.model;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@AllArgsConstructor
@NoArgsConstructor
public class ReconciledPaymentsLastSixMonthsDTO {
    private String month;
    private Long totalReconciled;
}
