package finance.backend.bvnos.service;

import finance.backend.bvnos.model.*;
import finance.backend.bvnos.repository.UserRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;

import java.time.LocalDateTime;
import java.util.Optional;

@Service
@RequiredArgsConstructor
public class UserServiceImpl  implements UserService {

    private final UserRepository userRepository;

    private final PasswordEncoder passwordEncoder;

    @Override
    public UserResponseDTO createUser(CreateUserRequestDTO requestDTO) {

        Optional<User> existingUser =
                userRepository.findByEmailAndActiveTrue(
                        requestDTO.getEmail()
                );

        if (existingUser.isPresent()) {
            throw new RuntimeException("User already exists");
        }

        User user = new User();

        user.setFirstName(requestDTO.getFirstName());

        user.setLastName(requestDTO.getLastName());

        user.setEmail(
                requestDTO.getEmail()
                        .trim()
                        .toLowerCase()
        );

        user.setPasswordHash(
                passwordEncoder.encode(
                        requestDTO.getPassword())
        );

        user.setArea(requestDTO.getArea());

        user.setRole(requestDTO.getRole());

        user.setActive(true);

        user.setCreatedAt(LocalDateTime.now());

        user.setUpdatedAt(LocalDateTime.now());

        userRepository.save(user);

        return mapUser(user);
    }

    @Override
    public LoginResponseDTO login(LoginRequestDTO requestDTO) {

        User user =
                userRepository
                        .findByEmailAndActiveTrue(
                                requestDTO.getEmail()
                                        .trim()
                                        .toLowerCase()
                        )
                        .orElseThrow(() -> new RuntimeException("Invalid credentials"));

        boolean passwordMatches =
                passwordEncoder.matches(
                        requestDTO.getPassword(),
                        user.getPasswordHash());

        if (!passwordMatches) {
            throw new RuntimeException("Invalid credentials");
        }

        return LoginResponseDTO.builder()
                .id(user.getId())
                .firstName(user.getFirstName())
                .lastName(user.getLastName())
                .email(user.getEmail())
                .area(user.getArea())
                .role(user.getRole())
                .build();
    }

    private UserResponseDTO mapUser(User user) {

        return UserResponseDTO.builder()
                .id(user.getId())
                .firstName(user.getFirstName())
                .lastName(user.getLastName())
                .email(user.getEmail())
                .area(user.getArea())
                .role(user.getRole())
                .active(user.getActive())
                .createdAt(user.getCreatedAt())
                .build();
    }

    @Override
    public UserResponseDTO updateUser(String id, UpdateUserRequestDTO requestDTO) {

        User user = userRepository.findById(id).orElseThrow(() -> new RuntimeException("User not found"));

        if (!user.getActive()) {
            throw new RuntimeException("User is inactive");
        }

        Optional<User> existingUser =
                userRepository.findByEmailAndActiveTrue(
                        requestDTO.getEmail()
                                .trim()
                                .toLowerCase()
                );

        if (existingUser.isPresent() && !existingUser.get().getId().equals(id)) {
            throw new RuntimeException("Email already in use");
        }

        user.setFirstName(requestDTO.getFirstName());

        user.setLastName(requestDTO.getLastName());

        user.setEmail(
                requestDTO.getEmail()
                        .trim()
                        .toLowerCase()
        );

        user.setArea(requestDTO.getArea());

        user.setUpdatedAt(LocalDateTime.now());

        userRepository.save(user);

        return mapUser(user);
    }
}
