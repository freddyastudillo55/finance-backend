package finance.backend.bvnos.exception;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;

import java.util.Map;

import static org.assertj.core.api.Assertions.assertThat;

class GlobalExceptionHandlerTest {

    private GlobalExceptionHandler exceptionHandler;

    @BeforeEach
    void setUp() {
        exceptionHandler = new GlobalExceptionHandler();
    }

    @Test
    @DisplayName("Should return 401 for Invalid credentials")
    void shouldReturn401ForInvalidCredentials() {
        RuntimeException ex = new RuntimeException("Invalid credentials");

        ResponseEntity<Map<String, String>> response = exceptionHandler.handleRuntimeException(ex);

        assertThat(response.getStatusCode()).isEqualTo(HttpStatus.UNAUTHORIZED);
        assertThat(response.getBody()).containsEntry("message", "Invalid credentials");
    }

    @Test
    @DisplayName("Should return 401 for User already exists")
    void shouldReturn401ForUserAlreadyExists() {
        RuntimeException ex = new RuntimeException("User already exists");

        ResponseEntity<Map<String, String>> response = exceptionHandler.handleRuntimeException(ex);

        assertThat(response.getStatusCode()).isEqualTo(HttpStatus.UNAUTHORIZED);
        assertThat(response.getBody()).containsEntry("message", "User already exists");
    }

    @Test
    @DisplayName("Should return 400 for other runtime exceptions")
    void shouldReturn400ForOtherExceptions() {
        RuntimeException ex = new RuntimeException("Result not found");

        ResponseEntity<Map<String, String>> response = exceptionHandler.handleRuntimeException(ex);

        assertThat(response.getStatusCode()).isEqualTo(HttpStatus.BAD_REQUEST);
        assertThat(response.getBody()).containsEntry("message", "Result not found");
    }

    @Test
    @DisplayName("Should return 400 with default message when exception message is null")
    void shouldReturn400WithDefaultMessageForNullMessage() {
        RuntimeException ex = new RuntimeException();

        ResponseEntity<Map<String, String>> response = exceptionHandler.handleRuntimeException(ex);

        assertThat(response.getStatusCode()).isEqualTo(HttpStatus.BAD_REQUEST);
        assertThat(response.getBody()).containsEntry("message", "An unexpected error occurred");
    }
}
