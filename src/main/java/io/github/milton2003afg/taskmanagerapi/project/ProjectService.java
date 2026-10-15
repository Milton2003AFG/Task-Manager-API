package io.github.milton2003afg.taskmanagerapi.project;

import io.github.milton2003afg.taskmanagerapi.exception.ResourceNotFoundException;
import io.github.milton2003afg.taskmanagerapi.exception.UserNotFoundException;
import io.github.milton2003afg.taskmanagerapi.user.User;
import io.github.milton2003afg.taskmanagerapi.user.UserRepository;
import org.springframework.stereotype.Service;

import java.util.List;

@Service
public class ProjectService {
    private final ProjectRepository projectRepository;
    private final UserRepository userRepository;

    public ProjectService(ProjectRepository projectRepository, UserRepository userRepository){
        this.projectRepository = projectRepository;
        this.userRepository = userRepository;
    }

    public List<ProjectResponse> getAll(long ownerId){
        return projectRepository.findByOwnerId(ownerId).stream()
                .map(ProjectResponse::from)
                .toList();
    }

    public ProjectResponse create(long userId, ProjectRequest request){
        User user = userRepository.findById(userId).orElseThrow(() -> new UserNotFoundException("Invalid authentication"));
        return ProjectResponse.from(projectRepository.save(new Project(request.name(), user)));
    }

    public ProjectResponse getById(long projectId, long ownerId){
        return ProjectResponse.from(projectRepository.findByIdAndOwnerId(projectId, ownerId)
                .orElseThrow(() -> new ResourceNotFoundException("The project does not exist")));
    }

    public ProjectResponse update(long projectId, long ownerId, ProjectRequest request){
        Project project = projectRepository.findByIdAndOwnerId(projectId, ownerId).orElseThrow(() -> new ResourceNotFoundException("The project does not exist"));
        project.setName(request.name());
        return ProjectResponse.from(projectRepository.save(project));
    }

    public void delete(long projectId, long ownerId){
        Project project = projectRepository.findByIdAndOwnerId(projectId, ownerId).orElseThrow(() -> new ResourceNotFoundException("The project does not exist"));
        projectRepository.delete(project);
    }
}
