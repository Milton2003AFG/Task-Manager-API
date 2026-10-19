package io.github.milton2003afg.taskmanagerapi;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.webmvc.test.autoconfigure.AutoConfigureMockMvc;
import org.springframework.context.annotation.Import;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.test.web.servlet.MockMvc;
import com.jayway.jsonpath.JsonPath;
import org.springframework.http.MediaType;

import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultHandlers.print;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

import java.util.UUID;

@SpringBootTest
@AutoConfigureMockMvc
@ActiveProfiles("test")
@Import(TestcontainersConfiguration.class)
public abstract class AbstractIntegrationTest {
    protected static final String PASSWORD = "Abcdefghij12#";
    @Autowired protected MockMvc mockMvc;

    protected String uniqueEmail(){
        return "user" + UUID.randomUUID() + "@test.com";
    }

    protected String registerAndLogin(String email, String password) throws Exception{
        String credentials = """
                {"email":"%s", "password":"%s"}
                """.formatted(email, password);

        mockMvc.perform(post("/auth/register")
                        .contentType(MediaType.APPLICATION_JSON)
                .content(credentials))
                .andExpect(status().isCreated());

        String response = mockMvc.perform(post("/auth/login")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(credentials))
                .andExpect(status().isOk())
                .andReturn()
                .getResponse()
                .getContentAsString();
        return JsonPath.read(response, "$.accessToken");
    }

    protected Integer createProject(String token, String name) throws Exception{
        String request = """
                {"name":"%s"}
                """.formatted(name);
        String response = mockMvc.perform(post("/projects")
                                .contentType(MediaType.APPLICATION_JSON)
                                .content(request)
                .header("Authorization", "Bearer " + token))
                .andExpect(status().isCreated())
                .andReturn()
                .getResponse()
                .getContentAsString();
        return JsonPath.read(response, "$.id");
    }

    protected Integer createTask(String token, long projectId, String title) throws Exception{
        String request = """
                {"title":"%s"}
                """.formatted(title);
        String response = mockMvc.perform(post("/projects/{projectId}/tasks", projectId)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(request)
                .header("Authorization", "Bearer " + token))
                .andDo(print())
                .andExpect(status().isCreated())
                .andReturn()
                .getResponse()
                .getContentAsString();
        return JsonPath.read(response, "$.id");
    }
}
