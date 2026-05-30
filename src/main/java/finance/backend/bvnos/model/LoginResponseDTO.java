package finance.backend.bvnos.model;

import lombok.Builder;
import lombok.Data;

@Data
@Builder
public class LoginResponseDTO {
    private String id;
    private String firstName;
    private String lastName;
    private String email;
    private String area;
    private String role;
}
