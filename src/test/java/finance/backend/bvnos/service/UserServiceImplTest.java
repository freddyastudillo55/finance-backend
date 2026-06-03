package finance.backend.bvnos.service;

import finance.backend.bvnos.model.*;
import finance.backend.bvnos.repository.UserRepository;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.ArgumentCaptor;
import org.mockito.Captor;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.security.crypto.password.PasswordEncoder;

import java.time.LocalDateTime;
import java.util.Optional;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class UserServiceImplTest {

    @Mock
    private UserRepository userRepository;

    @Mock
    private PasswordEncoder passwordEncoder;

    @InjectMocks
    private UserServiceImpl userService;

    @Captor
    private ArgumentCaptor<User> userCaptor;

    private CreateUserRequestDTO createUserRequestDTO;
    private LoginRequestDTO loginRequestDTO;
    private UpdateUserRequestDTO updateUserRequestDTO;
    private User existingUser;

    @BeforeEach
    void setUp() {
        createUserRequestDTO = new CreateUserRequestDTO();
        createUserRequestDTO.setFirstName("John");
        createUserRequestDTO.setLastName("Doe");
        createUserRequestDTO.setEmail("  JOHN@example.com  ");
        createUserRequestDTO.setPassword("password123");
        createUserRequestDTO.setArea("Software");
        createUserRequestDTO.setRole("USER");

        loginRequestDTO = new LoginRequestDTO();
        loginRequestDTO.setEmail("john@example.com");
        loginRequestDTO.setPassword("password123");

        updateUserRequestDTO = new UpdateUserRequestDTO();
        updateUserRequestDTO.setFirstName("Jane");
        updateUserRequestDTO.setLastName("Smith");
        updateUserRequestDTO.setEmail("  JANE@example.com  ");
        updateUserRequestDTO.setArea("Accounting");

        existingUser = new User();
        existingUser.setId("user-123");
        existingUser.setFirstName("John");
        existingUser.setLastName("Doe");
        existingUser.setEmail("john@example.com");
        existingUser.setPasswordHash("encoded-password");
        existingUser.setArea("Software");
        existingUser.setRole("USER");
        existingUser.setActive(true);
        existingUser.setCreatedAt(LocalDateTime.now().minusDays(1));
        existingUser.setUpdatedAt(LocalDateTime.now().minusDays(1));
    }

    @Nested
    @DisplayName("createUser tests")
    class CreateUserTests {

        @Test
        @DisplayName("Should create user successfully when email is not taken")
        void shouldCreateUserSuccessfully() {
            when(userRepository.findByEmailAndActiveTrue("john@example.com")).thenReturn(Optional.empty());
            when(passwordEncoder.encode("password123")).thenReturn("encoded-password");
            when(userRepository.save(any(User.class))).thenAnswer(invocation -> {
                User saved = invocation.getArgument(0);
                saved.setId("new-user-id");
                return saved;
            });

            UserResponseDTO result = userService.createUser(createUserRequestDTO);

            assertThat(result).isNotNull();
            assertThat(result.getId()).isEqualTo("new-user-id");
            assertThat(result.getFirstName()).isEqualTo("John");
            assertThat(result.getLastName()).isEqualTo("Doe");
            assertThat(result.getEmail()).isEqualTo("john@example.com");
            assertThat(result.getArea()).isEqualTo("Software");
            assertThat(result.getRole()).isEqualTo("USER");
            assertThat(result.getActive()).isTrue();
            assertThat(result.getCreatedAt()).isNotNull();

            verify(userRepository).findByEmailAndActiveTrue("john@example.com");
            verify(passwordEncoder).encode("password123");

            verify(userRepository).save(userCaptor.capture());
            User savedUser = userCaptor.getValue();
            assertThat(savedUser.getEmail()).isEqualTo("john@example.com");
            assertThat(savedUser.getPasswordHash()).isEqualTo("encoded-password");
            assertThat(savedUser.getActive()).isTrue();
        }

        @Test
        @DisplayName("Should throw exception when email already exists")
        void shouldThrowExceptionWhenEmailAlreadyExists() {
            when(userRepository.findByEmailAndActiveTrue("john@example.com")).thenReturn(Optional.of(existingUser));

            assertThatThrownBy(() -> userService.createUser(createUserRequestDTO))
                    .isInstanceOf(RuntimeException.class)
                    .hasMessage("User already exists");

            verify(userRepository).findByEmailAndActiveTrue("john@example.com");
            verify(userRepository, never()).save(any());
            verify(passwordEncoder, never()).encode(anyString());
        }

        @Test
        @DisplayName("Should trim and lowercase email before checking")
        void shouldTrimAndLowercaseEmail() {
            when(userRepository.findByEmailAndActiveTrue("john@example.com")).thenReturn(Optional.empty());
            when(passwordEncoder.encode(anyString())).thenReturn("encoded");
            when(userRepository.save(any(User.class))).thenAnswer(inv -> {
                User u = inv.getArgument(0);
                u.setId("id-1");
                return u;
            });

            userService.createUser(createUserRequestDTO);

            verify(userRepository).findByEmailAndActiveTrue("john@example.com");
            verify(userRepository).save(userCaptor.capture());
            assertThat(userCaptor.getValue().getEmail()).isEqualTo("john@example.com");
        }
    }

    @Nested
    @DisplayName("login tests")
    class LoginTests {

        @Test
        @DisplayName("Should login successfully with valid credentials")
        void shouldLoginSuccessfully() {
            when(userRepository.findByEmailAndActiveTrue("john@example.com")).thenReturn(Optional.of(existingUser));
            when(passwordEncoder.matches("password123", "encoded-password")).thenReturn(true);

            LoginResponseDTO result = userService.login(loginRequestDTO);

            assertThat(result).isNotNull();
            assertThat(result.getId()).isEqualTo("user-123");
            assertThat(result.getFirstName()).isEqualTo("John");
            assertThat(result.getLastName()).isEqualTo("Doe");
            assertThat(result.getEmail()).isEqualTo("john@example.com");
            assertThat(result.getArea()).isEqualTo("Software");
            assertThat(result.getRole()).isEqualTo("USER");

            verify(userRepository).findByEmailAndActiveTrue("john@example.com");
            verify(passwordEncoder).matches("password123", "encoded-password");
        }

        @Test
        @DisplayName("Should throw exception when user not found")
        void shouldThrowExceptionWhenUserNotFound() {
            when(userRepository.findByEmailAndActiveTrue("john@example.com")).thenReturn(Optional.empty());

            assertThatThrownBy(() -> userService.login(loginRequestDTO))
                    .isInstanceOf(RuntimeException.class)
                    .hasMessage("Invalid credentials");

            verify(userRepository).findByEmailAndActiveTrue("john@example.com");
            verify(passwordEncoder, never()).matches(anyString(), anyString());
        }

        @Test
        @DisplayName("Should throw exception when password does not match")
        void shouldThrowExceptionWhenPasswordDoesNotMatch() {
            when(userRepository.findByEmailAndActiveTrue("john@example.com")).thenReturn(Optional.of(existingUser));
            when(passwordEncoder.matches("password123", "encoded-password")).thenReturn(false);

            assertThatThrownBy(() -> userService.login(loginRequestDTO))
                    .isInstanceOf(RuntimeException.class)
                    .hasMessage("Invalid credentials");

            verify(userRepository).findByEmailAndActiveTrue("john@example.com");
            verify(passwordEncoder).matches("password123", "encoded-password");
        }

        @Test
        @DisplayName("Should trim and lowercase email before login")
        void shouldTrimAndLowercaseEmail() {
            loginRequestDTO.setEmail("  JOHN@EXAMPLE.COM  ");
            when(userRepository.findByEmailAndActiveTrue("john@example.com")).thenReturn(Optional.of(existingUser));
            when(passwordEncoder.matches(anyString(), anyString())).thenReturn(true);

            userService.login(loginRequestDTO);

            verify(userRepository).findByEmailAndActiveTrue("john@example.com");
        }
    }

    @Nested
    @DisplayName("updateUser tests")
    class UpdateUserTests {

        @Test
        @DisplayName("Should update user successfully")
        void shouldUpdateUserSuccessfully() {
            String userId = "user-123";
            when(userRepository.findById(userId)).thenReturn(Optional.of(existingUser));
            when(userRepository.findByEmailAndActiveTrue("jane@example.com")).thenReturn(Optional.empty());
            when(userRepository.save(any(User.class))).thenAnswer(inv -> inv.getArgument(0));

            UserResponseDTO result = userService.updateUser(userId, updateUserRequestDTO);

            assertThat(result).isNotNull();
            assertThat(result.getFirstName()).isEqualTo("Jane");
            assertThat(result.getLastName()).isEqualTo("Smith");
            assertThat(result.getEmail()).isEqualTo("jane@example.com");
            assertThat(result.getArea()).isEqualTo("Accounting");

            verify(userRepository).findById(userId);
            verify(userRepository).findByEmailAndActiveTrue("jane@example.com");
            verify(userRepository).save(userCaptor.capture());
            User savedUser = userCaptor.getValue();
            assertThat(savedUser.getFirstName()).isEqualTo("Jane");
            assertThat(savedUser.getEmail()).isEqualTo("jane@example.com");
        }

        @Test
        @DisplayName("Should throw exception when user not found")
        void shouldThrowExceptionWhenUserNotFound() {
            when(userRepository.findById("invalid-id")).thenReturn(Optional.empty());

            assertThatThrownBy(() -> userService.updateUser("invalid-id", updateUserRequestDTO))
                    .isInstanceOf(RuntimeException.class)
                    .hasMessage("User not found");

            verify(userRepository).findById("invalid-id");
            verify(userRepository, never()).save(any());
        }

        @Test
        @DisplayName("Should throw exception when user is inactive")
        void shouldThrowExceptionWhenUserInactive() {
            existingUser.setActive(false);
            when(userRepository.findById("user-123")).thenReturn(Optional.of(existingUser));

            assertThatThrownBy(() -> userService.updateUser("user-123", updateUserRequestDTO))
                    .isInstanceOf(RuntimeException.class)
                    .hasMessage("User is inactive");

            verify(userRepository).findById("user-123");
            verify(userRepository, never()).save(any());
        }

        @Test
        @DisplayName("Should throw exception when email already in use by another user")
        void shouldThrowExceptionWhenEmailAlreadyInUse() {
            User otherUser = new User();
            otherUser.setId("other-user-id");
            otherUser.setEmail("jane@example.com");
            otherUser.setActive(true);

            when(userRepository.findById("user-123")).thenReturn(Optional.of(existingUser));
            when(userRepository.findByEmailAndActiveTrue("jane@example.com")).thenReturn(Optional.of(otherUser));

            assertThatThrownBy(() -> userService.updateUser("user-123", updateUserRequestDTO))
                    .isInstanceOf(RuntimeException.class)
                    .hasMessage("Email already in use");

            verify(userRepository).findById("user-123");
            verify(userRepository).findByEmailAndActiveTrue("jane@example.com");
            verify(userRepository, never()).save(any());
        }

        @Test
        @DisplayName("Should allow updating to same email (own email)")
        void shouldAllowUpdatingToSameEmail() {
            existingUser.setEmail("jane@example.com");
            when(userRepository.findById("user-123")).thenReturn(Optional.of(existingUser));
            when(userRepository.findByEmailAndActiveTrue("jane@example.com")).thenReturn(Optional.of(existingUser));
            when(userRepository.save(any(User.class))).thenAnswer(inv -> inv.getArgument(0));

            UserResponseDTO result = userService.updateUser("user-123", updateUserRequestDTO);

            assertThat(result).isNotNull();
            assertThat(result.getEmail()).isEqualTo("jane@example.com");

            verify(userRepository).save(any(User.class));
        }
    }
}
