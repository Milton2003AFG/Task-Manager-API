package io.github.milton2003afg.taskmanagerapi.task;

import io.github.milton2003afg.taskmanagerapi.exception.ResourceNotFoundException;
import io.github.milton2003afg.taskmanagerapi.project.Project;
import io.github.milton2003afg.taskmanagerapi.project.ProjectRepository;
import jakarta.transaction.Transactional;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;

import java.util.EnumMap;
import java.util.Map;

@Service
public class TaskService {
    private final TaskRepository taskRepository;
    private final ProjectRepository projectRepository;

    public TaskService(TaskRepository taskRepository, ProjectRepository projectRepository) {
        this.taskRepository = taskRepository;
        this.projectRepository = projectRepository;
    }

    public TaskResponse create(Long projectId, Long ownerId, TaskRequest request){
        Project project = projectRepository.findByIdAndOwnerId(projectId, ownerId).orElseThrow(() -> new ResourceNotFoundException("The project does not exist"));
        return TaskResponse.from(taskRepository.save(new Task(request.title(), request.description(), request.dueDate(), project)));
    }

    public TaskResponse getByTaskId(Long taskId, Long projectId, Long ownerId){
        projectRepository.findByIdAndOwnerId(projectId, ownerId).orElseThrow(() -> new ResourceNotFoundException("The project does not exist"));
        return TaskResponse.from(taskRepository.findByIdAndProjectIdAndProjectOwnerId(taskId, projectId, ownerId)
                .orElseThrow(() -> new ResourceNotFoundException("The task does not exist")));
    }

    public Page<TaskResponse> getTasks(Long projectId, Long ownerId, TaskStatus status, Pageable pageable){
        projectRepository.findByIdAndOwnerId(projectId, ownerId).orElseThrow(() -> new ResourceNotFoundException("The project does not exist"));
        return taskRepository.findTasks(projectId, ownerId, status, pageable).map(TaskResponse::from);
    }

    public TaskStatsResponse getStats(Long projectId, Long ownerId){
        projectRepository.findByIdAndOwnerId(projectId, ownerId).orElseThrow(() -> new ResourceNotFoundException("The project does not exist"));
        Map<TaskStatus, Long> stats = new EnumMap<>(TaskStatus.class);
        for (TaskStatus status : TaskStatus.values()){
            stats.put(status, 0L);
        }

        taskRepository.countTasksByStatusPerProject(projectId, ownerId)
                .forEach(task -> {
                    stats.put(task.getStatus(), task.getTotal());
                });
        return new TaskStatsResponse(stats.get(TaskStatus.TODO), stats.get(TaskStatus.DOING), stats.get(TaskStatus.DONE));
    }

    @Transactional
    public TaskResponse update(Long taskId, Long projectId, Long ownerId, TaskUpdateRequest request){
        Task task = taskRepository.findByIdAndProjectIdAndProjectOwnerId(taskId, projectId, ownerId)
                .orElseThrow(() -> new ResourceNotFoundException("The resource does not exist"));

        task.setTitle(request.title() == null || request.title().isBlank() ? task.getTitle() : request.title());
        if (request.description() != null) {
            task.setDescription(request.description().isBlank() ? null : request.description());
        }
        task.setStatus(request.status() == null ? task.getStatus() : request.status());
        task.setDueDate(request.dueDate() == null ? task.getDueDate() : request.dueDate());
        return TaskResponse.from(taskRepository.save(task));
    }

    @Transactional
    public void delete(Long taskId, Long projectId, Long ownerId){
        Task task = taskRepository.findByIdAndProjectIdAndProjectOwnerId(taskId, projectId, ownerId)
                .orElseThrow(() -> new ResourceNotFoundException("The resource does not exist"));

        taskRepository.delete(task);
    }
}
