package io.github.milton2003afg.taskmanagerapi.task;

import jakarta.validation.constraints.Size;

import java.time.LocalDateTime;

public record TaskUpdateRequest(
        @Size(max = 200, message = "Title must be at most 200 characters")
        String title,

        @Size(max = 2000, message = "Description must be at most 2000 characters")
        String description,
        TaskStatus status,
        LocalDateTime dueDate
) {
}
