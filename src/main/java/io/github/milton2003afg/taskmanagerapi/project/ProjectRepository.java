package io.github.milton2003afg.taskmanagerapi.project;

import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;
import java.util.Optional;

public interface ProjectRepository extends JpaRepository<Project, Long> {
    Optional<Project>findByIdAndOwnerId(Long id, Long ownerId);
    List<Project> findByOwnerId(Long ownerId);
}
