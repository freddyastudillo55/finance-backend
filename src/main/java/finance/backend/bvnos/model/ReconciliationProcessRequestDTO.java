package finance.backend.bvnos.model;

import lombok.Data;
import org.springframework.web.multipart.MultipartFile;

@Data
public class ReconciliationProcessRequestDTO {
    private String name;
    private MultipartFile fileA;
    private MultipartFile fileB;
}
