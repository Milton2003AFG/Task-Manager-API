package io.github.milton2003afg.taskmanagerapi;

import org.junit.jupiter.api.Test;
import org.springframework.http.MediaType;

import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.*;
import static org.springframework.test.web.servlet.result.MockMvcResultHandlers.print;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

public class ProjectAndTaskIntegrationTest extends AbstractIntegrationTest{
    @Test
    void user_cannotAccessAnotherUsersTask()throws Exception{
        String emailA = uniqueEmail();
        String emailB = uniqueEmail();

        String tokenA = registerAndLogin(emailA, PASSWORD);
        String tokenB = registerAndLogin(emailB, PASSWORD);

        int idProjectA = createProject(tokenA, "Project Test A");
        int idTaskA = createTask(tokenA, idProjectA, "Task Test A");
        int idProjectB = createProject(tokenB, "Project Test B");
        int idTaskB = createTask(tokenB, idProjectB, "Task Test B");

        String requestPatch = """
                {"title":"Change title"}
                """;

        mockMvc.perform(get("/projects/{projectId}/tasks/{taskId}", idProjectB, idTaskA)
                .header("Authorization", "Bearer " + tokenB))
                .andExpect(status().isNotFound());

        mockMvc.perform(patch("/projects/{projectId}/tasks/{taskId}", idProjectB, idTaskA)
                .contentType(MediaType.APPLICATION_JSON)
                .content(requestPatch)
                .header("Authorization", "Bearer " + tokenB))
                .andDo(print())
                .andExpect(status().isNotFound());

        mockMvc.perform(delete("/projects/{projectId}/tasks/{taskId}", idProjectB, idTaskA)
                .header("Authorization", "Bearer " + tokenB))
                .andDo(print())
                .andExpect(status().isNotFound());

        mockMvc.perform(get("/projects/{projectId}/tasks/{taskId}", idProjectA, idTaskA)
                .header("Authorization", "Bearer " + tokenA))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.title").value("Task Test A"))
                .andReturn()
                .getResponse()
                .getContentAsString();

        mockMvc.perform(get("/projects/{idProject}/tasks/stats", idProjectA)
                .header("Authorization", "Bearer " + tokenB))
                .andExpect(status().isNotFound());
    }

    @Test
    void test_partialPatch() throws Exception{
        String email = uniqueEmail();
        String token = registerAndLogin(email, PASSWORD);
        int idProject = createProject(token, "Project Test");
        int idTask = createCompleteTask(token, idProject, "Task Test", "Description for task", "2026-12-31T18:00:00");

        String requestPatch = """
                {"status":"DOING"}
                """;

        mockMvc.perform(patch("/projects/{projectId}/tasks/{taskId}", idProject, idTask)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(requestPatch)
                        .header("Authorization", "Bearer " + token))
                .andDo(print())
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.title").value("Task Test"))
                .andExpect(jsonPath("$.description").value("Description for task"))
                .andExpect(jsonPath("$.status").value("DOING"))
                .andExpect(jsonPath("$.dueDate").value("2026-12-31T18:00:00"));

        mockMvc.perform(get("/projects/{projectId}/tasks/{taskId}", idProject, idTask)
                .header("Authorization", "Bearer " + token))
                .andDo(print())
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.title").value("Task Test"))
                .andExpect(jsonPath("$.description").value("Description for task"))
                .andExpect(jsonPath("$.status").value("DOING"))
                .andExpect(jsonPath("$.dueDate").value("2026-12-31T18:00:00"));
    }

    @Test
    void test_Stats() throws Exception{
        String email = uniqueEmail();
        String token = registerAndLogin(email, PASSWORD);
        int idProject = createProject(token, "Project Test");
        createCompleteTask(token, idProject, "Task Test", "Description for task", "2026-12-31T18:00:00");
        int idTask2 = createCompleteTask(token, idProject, "Task Test", "Description for task", "2026-12-31T18:00:00");
        int idTask3 = createCompleteTask(token, idProject, "Task Test", "Description for task", "2026-12-31T18:00:00");

        String requestPatchDoing = """
                {"status":"DOING"}
                """;

        mockMvc.perform(patch("/projects/{projectId}/tasks/{taskId}", idProject, idTask2)
                        .header("Authorization", "Bearer " + token)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(requestPatchDoing))
                .andExpect(status().isOk());
        mockMvc.perform(patch("/projects/{projectId}/tasks/{taskId}", idProject, idTask3)
                        .header("Authorization", "Bearer " + token)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(requestPatchDoing))
                .andExpect(status().isOk());

        mockMvc.perform(get("/projects/{projectId}/tasks/stats", idProject)
                .header("Authorization", "Bearer " + token))
                .andDo(print())
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.todo").value(1))
                .andExpect(jsonPath("$.doing").value(2))
                .andExpect(jsonPath("$.done").value(0));
    }
}
