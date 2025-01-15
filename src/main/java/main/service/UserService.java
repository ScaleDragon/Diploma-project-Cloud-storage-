package main.service;

import lombok.AllArgsConstructor;
import main.dto.request.LoginRequest;
import main.dto.response.LoginResponse;
import main.entity.User;
import main.exception.AuthorizationException;
import main.repository.UserRepository;
import main.utils.HashUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import javax.transaction.Transactional;
import java.security.NoSuchAlgorithmException;
import java.util.Arrays;
import java.util.Optional;

@Service
@AllArgsConstructor
public class UserService {
    @Autowired
    private UserRepository userRepository;
    @Autowired
    private SessionService sessionService;
    private final Logger logger = LoggerFactory.getLogger(UserService.class);

    /**
     * Совершает вход пользователя в систему.
     * @param loginRequest запрос входа пользователя
     * @return объект с токеном авторизации
     * @throws NoSuchAlgorithmException при внутренней ошибке хеширования пароля
     * @throws IllegalArgumentException при неверном логине или пароле
     */
    @Transactional
    public LoginResponse login(LoginRequest loginRequest) throws NoSuchAlgorithmException {
        String login = loginRequest.getLogin();
        String password = loginRequest.getPassword();
        Optional<User> user = userRepository.findByLogin(login);
        if (user.isEmpty()) {
            logger.error("Попытка входа под несуществующим логином " + login + "!");
            throw new AuthorizationException("Пользователь с логином " + login + " не существует!");
        }
        byte[] hashedPassword = HashUtils.hash(login, password);
        if (!Arrays.equals(hashedPassword, user.get().getPassword())) {
            logger.error("Ошибка авторизации: логин " + login + "!");
            throw new AuthorizationException("Неверный пароль для пользователя " + login + "!");
        }
        if (sessionService.findByUserId(user.get().getId()).isPresent()) {
            logger.error("Ошибка: пользователь с логином " + login + " уже вошёл в систему!");
            throw new IllegalStateException("Пользователь с логином " + login + " уже вошёл в систему!");
        }
        String authToken = sessionService.generateAuthToken(user.get().getId());
        logger.info("Вход в систему пользователя с логином " + login + ", токен = " + authToken + ".");
        return new LoginResponse(authToken);
    }

    /**
     * Совершает выход пользователя из системы.
     * @param authToken токен авторизации
     * @throws IllegalArgumentException если токен авторизации не привязан к пользователю
     */
    public void logout(String authToken) {
        logger.info("Выход из системы пользователя c токеном " + authToken + ".");
        sessionService.deleteAuthToken(authToken);
    }

    /**
     * Регистрирует нового пользователя.
     * @param login логин
     * @param password пароль
     * @throws NoSuchAlgorithmException при внутренней ошибке хеширования пароля
     */
    public void addUser(String login, String password) throws NoSuchAlgorithmException {
        if (userRepository.findByLogin(login).isPresent()) {
            logger.error("Ошибка: пользователь с логином " + login + " уже существует!");
            throw new IllegalArgumentException("Пользователь с логином " + login + " уже существует!");
        }
        User user = User.builder()
                .login(login)
                .password(HashUtils.hash(login, password))
                .build();
        logger.info("Регистрация пользователя с логином " + login + ".");
        userRepository.save(user);
    }
}
