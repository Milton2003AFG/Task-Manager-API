package io.github.milton2003afg.taskmanagerapi.task;

import jakarta.validation.Valid;
import org.springframework.data.domain.Pageable;
import org.springframework.data.web.PageableDefault;
import org.springframework.data.web.PagedModel;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.security.oauth2.jwt.Jwt;
import org.springframework.web.bind.annotation.*;
import java.util.Objects;

@RestController
@RequestMapping("/projects/{projectId}/tasks")
public class TaskController {
    private final TaskService taskService;

    public TaskController(TaskService taskService) {
        this.taskService = taskService;
    }

    private long currentUserId(Jwt jwt){
        return Long.parseLong(Objects.requireNonNull(jwt.getSubject()));
    }

    @PostMapping
    public ResponseEntity<TaskResponse> create(
            @AuthenticationPrincipal Jwt jwt,
            @PathVariable("projectId") long projectId,
            @Valid @RequestBody TaskRequest request){
        return ResponseEntity.status(HttpStatus.CREATED).body(taskService.create(projectId, currentUserId(jwt), request));
    }

    @GetMapping("/{taskId}")
    public ResponseEntity<TaskResponse> getByTaskId(
            @AuthenticationPrincipal Jwt jwt,
            @PathVariable("projectId") Long projectId,
            @PathVariable("taskId") Long taskId){
        return ResponseEntity.status(HttpStatus.OK).body(taskService.getByTaskId(taskId, projectId, currentUserId(jwt)));
    }

    @GetMapping
    public ResponseEntity<PagedModel<TaskResponse>> getTasks(
            @AuthenticationPrincipal Jwt jwt,
            @PathVariable("projectId") Long projectId,
            @RequestParam(required = false) TaskStatus status,
            @PageableDefault(size = 10, sort = "id") Pageable pageable){
        return ResponseEntity.status(HttpStatus.OK).body(new PagedModel<>(taskService.getTasks(projectId, currentUserId(jwt), status, pageable)));
    }

    @GetMapping("/stats")
    public ResponseEntity<TaskStatsResponse> getStats(
            @AuthenticationPrincipal Jwt jwt,
            @PathVariable("projectId") Long projectId){
        return ResponseEntity.status(HttpStatus.OK).body(taskService.getStats(projectId, currentUserId(jwt)));
    }

    @PatchMapping("/{taskId}")
    public ResponseEntity<TaskResponse> update(
            @AuthenticationPrincipal Jwt jwt,
            @PathVariable("taskId") Long taskId,
            @PathVariable("projectId") Long projectId,
            @Valid @RequestBody TaskUpdateRequest request){
        return ResponseEntity.status(HttpStatus.OK).body(taskService.update(taskId, projectId, currentUserId(jwt), request));
    }

    @DeleteMapping("/{taskId}")
    public ResponseEntity<Void> delete(
            @AuthenticationPrincipal Jwt jwt,
            @PathVariable("taskId") Long taskId,
            @PathVariable("projectId") Long projectId){
        taskService.delete(taskId, projectId, currentUserId(jwt));
        return ResponseEntity.status(HttpStatus.NO_CONTENT).build();
    }
}
