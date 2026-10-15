package io.github.milton2003afg.taskmanagerapi;

import org.springframework.boot.SpringApplication;

public class TestTaskManagerApiApplication {

    public static void main(String[] args) {
        SpringApplication.from(TaskManagerApiApplication::main).with(TestcontainersConfiguration.class).run(args);
    }

}
