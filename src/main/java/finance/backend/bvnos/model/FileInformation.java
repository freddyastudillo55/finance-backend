package finance.backend.bvnos.model;

import lombok.Data;

import java.time.LocalDateTime;

@Data
public class FileInformation {
    private String fileName;
    private LocalDateTime uploadedAt;
}
