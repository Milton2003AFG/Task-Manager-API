package io.github.milton2003afg.taskmanagerapi.task;

import java.time.LocalDateTime;

public record TaskResponse(Long id, String title, String description, TaskStatus status, LocalDateTime dueDate) {
    public static TaskResponse from(Task task){
        return new TaskResponse(task.getId(), task.getTitle(), task.getDescription(), task.getStatus(), task.getDueDate());
    }
}
