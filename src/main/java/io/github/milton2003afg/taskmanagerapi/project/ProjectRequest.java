package io.github.milton2003afg.taskmanagerapi.project;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Size;

public record ProjectRequest(
        @NotBlank(message = "The project's name is required")
        @Size(max = 150, message = "Name must be at most 150 characters")
        String name
) {
}
