package io.github.milton2003afg.taskmanagerapi.project;

import jakarta.validation.Valid;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.security.oauth2.jwt.Jwt;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.Objects;

@RestController
@RequestMapping("/project")
public class ProjectController {
    private final ProjectService projectService;

    public ProjectController(ProjectService projectService) {
        this.projectService = projectService;
    }

    private long currentUserId(Jwt jwt){
        return Long.parseLong(Objects.requireNonNull(jwt.getSubject()));
    }

    @GetMapping
    public ResponseEntity<List<ProjectResponse>> getAll(
            @AuthenticationPrincipal Jwt jwt){
        return ResponseEntity.status(HttpStatus.OK).body(projectService.getAll(currentUserId(jwt)));
    }

    @GetMapping("/{id}")
    public ResponseEntity<ProjectResponse> getById(
            @AuthenticationPrincipal Jwt jwt,
            @PathVariable("id") long id){
        return ResponseEntity.status(HttpStatus.OK).body(projectService.getById(id, currentUserId(jwt)));
    }

    @PostMapping
    public ResponseEntity<ProjectResponse> create (
            @AuthenticationPrincipal Jwt jwt,
            @Valid @RequestBody ProjectRequest projectRequest){
        return ResponseEntity.status(HttpStatus.CREATED).body(projectService.create(currentUserId(jwt), projectRequest));
    }

    @PutMapping("/{id}")
    public ResponseEntity<ProjectResponse> update(
            @AuthenticationPrincipal Jwt jwt,
            @PathVariable("id") long id,
            @Valid @RequestBody ProjectRequest projectRequest){
        return ResponseEntity.status(HttpStatus.OK).body(projectService.update(id, currentUserId(jwt), projectRequest));
    }

    @DeleteMapping("/{id}")
    public ResponseEntity<Void> delete(
            @AuthenticationPrincipal Jwt jwt,
            @PathVariable("id") long id){
        projectService.delete(id, currentUserId(jwt));
        return ResponseEntity.status(HttpStatus.NO_CONTENT).build();
    }
}
