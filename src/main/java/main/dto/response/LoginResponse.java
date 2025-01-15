package main.dto.response;

import com.fasterxml.jackson.annotation.JsonGetter;
import lombok.*;

/**
 * Результат входа пользователя: авторизационный токен.
 */
@Data
@Getter
@Setter
@AllArgsConstructor
@NoArgsConstructor
public class LoginResponse {
    /**
     * Авторизационный токен.
     */
    private String authToken;

    @JsonGetter("auth-token")
    public String getAuthToken() {
        return authToken;
    }
}
