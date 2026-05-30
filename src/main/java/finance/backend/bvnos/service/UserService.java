package finance.backend.bvnos.service;

import finance.backend.bvnos.model.*;

public interface UserService {

    UserResponseDTO createUser(CreateUserRequestDTO requestDTO);

    LoginResponseDTO login(LoginRequestDTO requestDTO);

    UserResponseDTO updateUser(String id, UpdateUserRequestDTO requestDTO);
}
