package finance.backend.bvnos.model;

import lombok.Builder;
import lombok.Data;

import java.time.LocalDateTime;

@Data
@Builder
public class UserResponseDTO {
    private String id;
    private String firstName;
    private String lastName;
    private String email;
    private String area;
    private String role;
    private Boolean active;
    private LocalDateTime createdAt;
}
