package io.github.milton2003afg.taskmanagerapi.project;


public record ProjectResponse(Long id, String name) {
    public static ProjectResponse from(Project project){
        return new ProjectResponse(project.getId(), project.getName());
    }
}
