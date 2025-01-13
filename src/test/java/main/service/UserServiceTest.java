package main.service;

import main.dto.request.LoginRequest;
import main.dto.response.FileResponse;
import main.entity.File;
import main.entity.Session;
import main.entity.User;
import main.exception.AuthorizationException;
import main.repository.FileRepository;
import main.repository.SessionRepository;
import main.repository.UserRepository;
import main.utils.HashUtils;
import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.orm.jpa.DataJpaTest;
import org.springframework.http.MediaType;
import org.springframework.mock.web.MockMultipartFile;
import org.springframework.test.context.TestPropertySource;
import org.springframework.test.context.junit.jupiter.SpringExtension;

import java.io.IOException;
import java.security.NoSuchAlgorithmException;
import java.util.Arrays;
import java.util.List;

@DataJpaTest
@ExtendWith(SpringExtension.class)
@TestPropertySource(locations = "classpath:application-test.properties")
public class UserServiceTest {
    @Autowired
    private UserRepository userRepository;
    @Autowired
    private SessionRepository sessionRepository;

    private SessionService sessionService;

    private UserService userService;

    @BeforeEach
    public void beforeEach() {
        sessionService = new SessionService(sessionRepository);
        userService = new UserService(userRepository, sessionService);

        sessionRepository.deleteAll();
        userRepository.deleteAll();
    }

    @Test
    @DisplayName("Добавление нового пользователя в систему")
    public void addUser() throws NoSuchAlgorithmException {
        userService.addUser("login", "pass");

        List<User> users = userRepository.findAll();
        Assertions.assertEquals(1, users.size());
        User user = users.get(0);
        Assertions.assertEquals("login", user.getLogin());
        Assertions.assertTrue(Arrays.equals(HashUtils.hash("login", "pass"), user.getPassword()));
    }

    @Test
    @DisplayName("Вход пользователя в систему")
    public void loginUser() throws NoSuchAlgorithmException {
        userService.addUser("login", "pass");

        User user = userRepository.findAll().get(0);

        Assertions.assertThrows(AuthorizationException.class, () -> {
            userService.login(new LoginRequest("login", "wrong"));
        });

        userService.login(new LoginRequest("login", "pass"));
        List<Session> sessions = sessionRepository.findAll();
        Assertions.assertEquals(1, sessions.size());
        Session session = sessions.get(0);
        Assertions.assertEquals(user.getId(), session.getUserId());

        // Повторный логин должен кидать ошибку
        Assertions.assertThrows(IllegalStateException.class, () -> {
            userService.login(new LoginRequest("login", "pass"));
        });
    }

    @Test
    @DisplayName("Выход пользователя из системы")
    public void logoutUser() throws NoSuchAlgorithmException {
        userService.addUser("login", "pass");
        String authToken = userService.login(new LoginRequest("login", "pass")).getAuthToken();
        userService.logout(authToken);

        Assertions.assertTrue(sessionRepository.findAll().isEmpty());
    }
}
