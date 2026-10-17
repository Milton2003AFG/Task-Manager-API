package io.github.milton2003afg.taskmanagerapi.task;

import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import java.util.List;
import java.util.Optional;

public interface TaskRepository extends JpaRepository<Task, Long> {
    Optional<Task> findByIdAndProjectIdAndProjectOwnerId(Long taskId, Long projectId, Long ownerId);
    @Query("SELECT t FROM Task t " +
            "WHERE t.project.id = :projectId " +
            "AND t.project.owner.id = :ownerId " +
            "AND (:status IS NULL OR t.status = :status)")
    Page<Task> findTasks(
            @Param("projectId") Long projectId,
            @Param("ownerId") Long ownerId,
            @Param("status") TaskStatus status,
            Pageable pageable);

    @Query("SELECT t.status AS status, COUNT(t) AS total FROM Task t " +
            "WHERE t.project.id = :projectId " +
            "AND t.project.owner.id = :ownerId " +
            "GROUP BY t.status")
    List<TaskStatusCount> countTasksByStatusPerProject(@Param("projectId") Long projectId, @Param("ownerId") Long ownerId);

}
