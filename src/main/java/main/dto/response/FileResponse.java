package main.dto.response;

import lombok.*;

/**
 * Объект с справкой по файлу.
 */
@Data
@Getter
@Setter
@AllArgsConstructor
@NoArgsConstructor
public class FileResponse {
    /**
     * Название файла.
     */
    private String filename;

    /**
     * Размер файла.
     */
    private Integer size;
}
