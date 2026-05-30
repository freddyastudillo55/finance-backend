package finance.backend.bvnos.model;

import lombok.Data;

import java.time.LocalDate;

@Data
public class SalesDetailsFilterDTO {
    private LocalDate startDate;
    private LocalDate endDate;
}
