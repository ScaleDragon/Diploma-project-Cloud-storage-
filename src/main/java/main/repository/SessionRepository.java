package main.repository;

import main.entity.Session;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.Optional;

@Repository
public interface SessionRepository extends JpaRepository<Session, Long> {
    /**
     * Получает сессию, при наличии, с данным токеном авторизации.
     * @param authToken токен авторизации
     * @return сессия, при наличии
     */
    Optional<Session> findByAuthToken(String authToken);

    /**
     * Получает сессию, при наличии, привязанную к данному пользователю.
     * @param userId идентификатор пользователя
     * @return сессия, при наличии
     */
    Optional<Session> findByUserId(Long userId);
}
