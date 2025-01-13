package main.integration;

import liquibase.pro.packaged.F;
import main.Application;
import main.dto.response.FileResponse;
import main.dto.response.LoginResponse;
import main.entity.File;
import main.entity.Session;
import main.entity.User;
import main.repository.FileRepository;
import main.repository.SessionRepository;
import main.repository.UserRepository;
import main.utils.HashUtils;
import org.junit.ClassRule;
import org.junit.Test;
import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.DisplayName;
import org.junit.runner.RunWith;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.test.util.TestPropertyValues;
import org.springframework.context.ApplicationContextInitializer;
import org.springframework.context.ConfigurableApplicationContext;
import org.springframework.core.io.ClassPathResource;
import org.springframework.core.io.Resource;
import org.springframework.http.MediaType;
import org.springframework.mock.web.MockMultipartFile;
import org.springframework.test.context.ContextConfiguration;
import org.springframework.test.context.junit4.SpringJUnit4ClassRunner;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.web.multipart.MultipartFile;
import org.testcontainers.containers.PostgreSQLContainer;
import org.testcontainers.shaded.com.fasterxml.jackson.databind.ObjectMapper;

import javax.annotation.processing.Filer;
import java.util.Arrays;
import java.util.List;

import static org.junit.Assert.assertNotNull;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.*;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

@RunWith(SpringJUnit4ClassRunner.class)
@AutoConfigureMockMvc
@SpringBootTest(classes = Application.class, webEnvironment = SpringBootTest.WebEnvironment.RANDOM_PORT)
@ContextConfiguration(initializers = IntegrationTest.Initializer.class)
public class IntegrationTest {
    @ClassRule
    public static PostgreSQLContainer postgres = new PostgreSQLContainer("postgres:latest");

    @Autowired
    private MockMvc mockMvc;

    @Autowired
    private FileRepository fileRepository;
    @Autowired
    private UserRepository userRepository;
    @Autowired
    private SessionRepository sessionRepository;

    private final ObjectMapper objectMapper = new ObjectMapper();

    public static class Initializer implements ApplicationContextInitializer<ConfigurableApplicationContext> {
        private static final String JDBC_URL = postgres.getJdbcUrl();

        @Override
        public void initialize(ConfigurableApplicationContext configurableApplicationContext) {
            TestPropertyValues values = TestPropertyValues.of(
                    "spring.datasource.url=" + JDBC_URL,
                    "spring.datasource.username=test",
                    "spring.datasource.password=test"
            );
            values.applyTo(configurableApplicationContext);
        }
    }

    @Test
    @DisplayName("Проверка флоу управления пользователями - регистрация/логин/логаут")
    public void testRegisterLoginLogout() throws Exception {
        userRepository.deleteAll();
        sessionRepository.deleteAll();

        // Регистрация пользователя
        mockMvc.perform(post("/register?login=user&password=pass"))
                .andExpect(status().isOk());
        // Проверяем, что пользователь появился в системе
        List<User> users = userRepository.findAll();
        Assertions.assertEquals(1, users.size());
        User user = users.get(0);
        Assertions.assertEquals("user", user.getLogin());
        Assertions.assertTrue(Arrays.equals(HashUtils.hash("user", "pass"), user.getPassword()));

        // Логин пользователя
        String result = mockMvc.perform(post("/login")
                .contentType(MediaType.APPLICATION_JSON)
                .content("{\"login\": \"user\", \"password\": \"pass\"}")
                .accept(MediaType.APPLICATION_JSON))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.auth-token").exists())
                .andReturn().getResponse().getContentAsString();
        // Проверяем того, что сохранился в БД токен авторизации
        LoginResponse loginResponse = objectMapper.readValue(result, LoginResponse.class);
        List<Session> sessions = sessionRepository.findAll();
        Assertions.assertEquals(1, sessions.size());
        Session session = sessions.get(0);
        Assertions.assertEquals(loginResponse.getAuthToken(), session.getAuthToken());
        Assertions.assertEquals(user.getId(), session.getUserId());

        // Выход пользователя из системы
        mockMvc.perform(post("/logout")
                .header("auth-token", "Bearer " + session.getAuthToken()))
                .andExpect(status().isOk());
        // Проверяем, что токен авторизации стёрся
        Assertions.assertEquals(0, sessionRepository.findAll().size());
    }

    @Test
    @DisplayName("Проверка, что попытка входа по неправильным данным даёт 4хх")
    public void testBadLogin() throws Exception {
        userRepository.deleteAll();
        sessionRepository.deleteAll();

        mockMvc.perform(post("/login")
                .contentType(MediaType.APPLICATION_JSON)
                .content("{\"login\": \"user\", \"password\": \"pass\"}")
                .accept(MediaType.APPLICATION_JSON))
                .andExpect(status().is4xxClientError());
    }

    @Test
    @DisplayName("Проверка флоу управления файлами - добавление/переименование/удаление/получение списка")
    public void manageFiles() throws Exception {
        userRepository.deleteAll();
        sessionRepository.deleteAll();

        // Регистрация юзера - проверяется в отдельном тесте, пока просто данные заводим
        mockMvc.perform(post("/register?login=user&password=pass"))
                .andExpect(status().isOk());

        mockMvc.perform(post("/login")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("{\"login\": \"user\", \"password\": \"pass\"}")
                        .accept(MediaType.APPLICATION_JSON))
                .andExpect(status().isOk());

        String authToken = sessionRepository.findAll().get(0).getAuthToken();

        byte[] bytes = "Какой-то текст".getBytes();
        MockMultipartFile file = new MockMultipartFile(
                "file",
                "file.txt",
                MediaType.TEXT_PLAIN_VALUE,
                bytes
        );

        // Запрос на добавление файла
        mockMvc.perform(multipart("/file?filename=file.txt")
                        .file(file)
                        .header("auth-token", "Bearer " + authToken)
                .accept(MediaType.APPLICATION_JSON))
                .andExpect(status().isOk());
        // Проверяем, что в БД появился наш файл
        List<File> files = fileRepository.findAll();
        Assertions.assertEquals(1, files.size());
        File f = files.get(0);
        Assertions.assertEquals("file.txt", f.getName());
        Assertions.assertTrue(Arrays.equals(bytes, f.getContent()));

        // Запрос на изменение имени файла
        mockMvc.perform(put("/file?filename=file.txt")
                        .header("auth-token", "Bearer " + authToken)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("{\"filename\": \"new.txt\"}"))
                .andExpect(status().isOk());
        // Проверяем, что в БД название файла изменилось, а содержимое такое же
        files = fileRepository.findAll();
        Assertions.assertEquals(1, files.size());
        f = files.get(0);
        Assertions.assertEquals("new.txt", f.getName());
        Assertions.assertTrue(Arrays.equals(bytes, f.getContent()));

        // Запрос на получение списка файлов
        String filesResponse = mockMvc.perform(get("/list?limit=1")
                .header("auth-token", "Bearer " + authToken)
                .accept(MediaType.APPLICATION_JSON))
                .andExpect(status().isOk())
                .andReturn().getResponse().getContentAsString()
                .replace("[", "")
                .replace("]", "");
        FileResponse fileResponse = objectMapper.readValue(filesResponse, FileResponse.class);
        Assertions.assertEquals("new.txt", fileResponse.getFilename());
        Assertions.assertEquals(bytes.length, fileResponse.getSize());

        // Запрос на удаление файла
        mockMvc.perform(delete("/file?filename=new.txt")
                        .header("auth-token", "Bearer " + authToken)
                        .contentType(MediaType.APPLICATION_JSON))
                .andExpect(status().isOk());
        // Проверяем, что в БД пусто
        Assertions.assertEquals(0, fileRepository.findAll().size());
    }
}
