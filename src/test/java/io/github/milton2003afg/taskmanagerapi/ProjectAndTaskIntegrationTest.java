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
    }
}
