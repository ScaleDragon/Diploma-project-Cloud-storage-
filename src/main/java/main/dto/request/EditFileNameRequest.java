package main.dto.request;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.Getter;
import lombok.NoArgsConstructor;

/**
 * Запрос на изменение имени файла.
 */
@Data
@Getter
@AllArgsConstructor
@NoArgsConstructor
public class EditFileNameRequest {
    /**
     * Новое имя файла.
     */
    private String filename;
}
