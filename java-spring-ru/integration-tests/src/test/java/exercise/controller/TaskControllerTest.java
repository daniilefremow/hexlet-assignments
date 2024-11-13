package exercise.controller;

import com.fasterxml.jackson.databind.ObjectMapper;
import exercise.model.Task;
import exercise.repository.TaskRepository;
import net.datafaker.Faker;
import org.instancio.Instancio;
import org.instancio.Select;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.http.MediaType;
import org.springframework.test.web.servlet.MockMvc;

import java.time.LocalDateTime;
import java.util.HashMap;

import static net.javacrumbs.jsonunit.assertj.JsonAssertions.assertThatJson;
import static org.assertj.core.api.Assertions.assertThat;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.delete;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.put;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

// BEGIN
@SpringBootTest
@AutoConfigureMockMvc
// END
class TaskControllerTest {

    @Autowired
    private MockMvc mockMvc;

    @Autowired
    private Faker faker;

    @Autowired
    private ObjectMapper om;

    @Autowired
    private TaskRepository taskRepository;


    @Test
    public void testWelcomePage() throws Exception {
        var result = mockMvc.perform(get("/"))
                .andExpect(status().isOk())
                .andReturn();

        var body = result.getResponse().getContentAsString();
        assertThat(body).contains("Welcome to Spring!");
    }

    @Test
    public void testIndex() throws Exception {
        var result = mockMvc.perform(get("/tasks"))
                .andExpect(status().isOk())
                .andReturn();

        var body = result.getResponse().getContentAsString();
        assertThatJson(body).isArray();
    }


    // BEGIN
    @Test
    public void testShow() throws Exception {
        var task = createTask();
        taskRepository.save(task);

        var result = mockMvc.perform(get("/tasks/" + task.getId()))
                .andExpect(status().isOk())
                .andReturn();
        var body = result.getResponse().getContentAsString();
        var taskFromDb = taskRepository.findByTitle(task.getTitle()).get();

        assertThatJson(body).and(
                a -> a.node("description").isEqualTo(taskFromDb.getDescription()),
                a -> a.node("createdAt").isEqualTo(taskFromDb.getCreatedAt().toString()),
                a -> a.node("updatedAt").isEqualTo(taskFromDb.getUpdatedAt().toString())
        );
    }

    @Test
    public void testCreate() throws Exception {
        var task = createTask();

        var request = post("/tasks")
                .contentType(MediaType.APPLICATION_JSON)
                .content(om.writeValueAsString(task));

        var response = mockMvc.perform(request)
                .andExpect(status().isCreated())
                .andReturn();
        var body = response.getResponse().getContentAsString();


        var taskFromDB = taskRepository.findByTitle(task.getTitle()).get();
        assertThatJson(body).and(
                a -> a.node("description").isEqualTo(taskFromDB.getDescription()),
                a -> a.node("createdAt").isEqualTo(taskFromDB.getCreatedAt().toString()),
                a -> a.node("updatedAt").isEqualTo(taskFromDB.getUpdatedAt().toString())
        );
    }

    @Test
    public void testUpdate() throws Exception {
        var task = createTask();
        taskRepository.save(task);

        var newTask = new HashMap<>();
        newTask.put("title", "title");
        newTask.put("description", "desc");


        var request = put("/tasks/" + task.getId())
                .contentType(MediaType.APPLICATION_JSON)
                .content(om.writeValueAsString(newTask));

        var response = mockMvc.perform(request)
                .andExpect(status().isOk())
                .andReturn();
        var body = response.getResponse().getContentAsString();


        var taskFromDB = taskRepository.findByTitle("title").get();
        assertThatJson(body).and(
                a -> a.node("description").isEqualTo(taskFromDB.getDescription()),
                a -> a.node("createdAt").isEqualTo(taskFromDB.getCreatedAt().toString()),
                a -> a.node("updatedAt").isEqualTo(taskFromDB.getUpdatedAt().toString())
        );
    }

    @Test
    public void testDelete() throws Exception {
        var task = createTask();
        taskRepository.save(task);

        mockMvc.perform(delete("/tasks/" + task.getId()))
                .andExpect(status().isOk());

        var taskFromDB = taskRepository.findByTitle(task.getTitle());
        assertThat(taskFromDB).isEmpty();
    }

    private Task createTask() {
        return Instancio.of(Task.class)
                .ignore(Select.field(Task::getId))
                .supply(Select.field(Task::getTitle), () -> faker.lorem().word())
                .supply(Select.field(Task::getDescription), () -> faker.lorem().sentence(5))
                .supply(Select.field(Task::getCreatedAt), () -> LocalDateTime.now().toLocalDate())
                .supply(Select.field(Task::getUpdatedAt), () -> LocalDateTime.now().toLocalDate())
                .create();
    }
    // END
}
