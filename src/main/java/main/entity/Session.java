package main.entity;

import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

import javax.persistence.*;

/**
 * Сущность «Сессия пользователя».
 */
@Entity
@Table(name = "sessions")
@Getter
@Setter
@AllArgsConstructor
@NoArgsConstructor
public class Session {
    /**
     * Идентификатор пользователя.
     */
    @Id
    @Column(name = "user_id")
    private Long userId;

    /**
     * Уникальный токен авторизации.
     */
    @Column(name = "auth_token")
    private String authToken;
}
