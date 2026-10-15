package io.github.milton2003afg.taskmanagerapi.auth;

import jakarta.validation.constraints.*;

public record UserRegisterRequest(
        @NotBlank(message = "Email is required")
        @Email(message = "Invalid email format")
        @Size(max = 255, message = "Email must be at most 255 characters")
        String email,

        @NotBlank(message = "Password is required")
        @Size(min = 12, max = 72, message = "Password must be between 12 and 72 characters")
        @Pattern(
                regexp = "^(?=.*[a-z])(?=.*[A-Z])(?=.*\\d)(?=.*[^A-Za-z0-9]).+$",
                message = "Password must contain uppercase, lowercase, number, and special character"
        )
        String password
) {}
