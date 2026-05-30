package finance.backend.bvnos.model;

import lombok.Data;

@Data
public class CreateUserRequestDTO {
    private String firstName;
    private String lastName;
    private String email;
    private String password;
    private String area;
    private String role;
}
