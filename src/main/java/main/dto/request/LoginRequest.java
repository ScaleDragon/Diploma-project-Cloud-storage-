package main.dto.request;

import lombok.*;

/**
 * Запрос на вход пользователя в систему.
 */
@Data
@Getter
@Setter
@AllArgsConstructor
@NoArgsConstructor
public class LoginRequest {
    /**
     * Логин.
     */
    private String login;

    /**
     * Пароль.
     */
    private String password;
}
