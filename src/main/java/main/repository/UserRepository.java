package main.repository;

import main.entity.User;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.Optional;

@Repository
public interface UserRepository extends JpaRepository<User, Long> {
    /**
     * Получает пользователя, при наличии, по логину.
     * @param login логин пользователя
     * @return пользователь, при наличии
     */
    Optional<User> findByLogin(String login);
}
