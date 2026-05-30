package finance.backend.bvnos.model;

import lombok.Data;

@Data
public class UpdateUserRequestDTO {
    private String firstName;
    private String lastName;
    private String email;
    private String area;
}
