package main.service;

import lombok.AllArgsConstructor;
import lombok.RequiredArgsConstructor;
import main.entity.File;
import main.entity.Session;
import main.repository.SessionRepository;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.util.Optional;
import java.util.UUID;

@Service
@AllArgsConstructor
public class SessionService {
    @Autowired
    private SessionRepository sessionRepository;
    private final Logger logger = LoggerFactory.getLogger(SessionService.class);

    /**
     * Генерирует и сохраняет в БД новый токен авторизации.
     * @param userId идентификатор пользователя, к которому привязан новый токен
     * @return сгенерированный токен
     */
    public String generateAuthToken(Long userId) {
        String authToken = UUID.randomUUID().toString();
        sessionRepository.save(new Session(userId, authToken));
        logger.info("Сгенерирован токен авторизации " + authToken + " для пользователя id = " + userId);
        return authToken;
    }

    /**
     * Удаляет токен авторизации из БД при выходе пользователя из системы.
     * @param authToken токен авторизации
     * @throws IllegalArgumentException если токен авторизации не привязан к пользователю
     */
    public void deleteAuthToken(String authToken) {
        Optional<Session> session = sessionRepository.findByAuthToken(authToken);
        if (session.isPresent()) {
            sessionRepository.delete(session.get());
            logger.info("Совершён выход пользователя id = " + session.get().getUserId() + " из системы.");
        } else {
            logger.error("Совершена некорректная попытка выхода по токену " + authToken + "!");
            throw new IllegalArgumentException("Неверный токен авторизации!");
        }
    }

    /**
     * Проверяет валидность токена авторизации и, если да, возвращает идентификатор
     * пользователя, к которому он привязан.
     * @param authToken токен авторизации
     * @return идентификатор пользователя, к которому привязан токен авторизации
     * @throws IllegalArgumentException если токен авторизации не привязан к пользователю
     */
    public Long checkAuthToken(String authToken) {
        Optional<Session> session = sessionRepository.findByAuthToken(authToken);
        if (session.isEmpty()) {
            logger.error("Некорректное действие в системе по токену " + authToken + "!");
            throw new IllegalArgumentException("Неверный токен авторизации!");
        }
        return session.get().getUserId();
    }

    /**
     * Получает сессию, при наличии, привязанную к данному пользователю.
     * @param userId идентификатор пользователя
     * @return сессия, при наличии
     */
    public Optional<Session> findByUserId(Long userId) {
        return sessionRepository.findByUserId(userId);
    }
}
