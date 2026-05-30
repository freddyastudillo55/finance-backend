package finance.backend.bvnos.model;

import lombok.Data;

import java.math.BigDecimal;
import java.time.LocalDateTime;

@Data
public class Adjustment {
    private Boolean applied;
    private String adjustedBy;
    private LocalDateTime adjustedAt;
    private String reason;
}
