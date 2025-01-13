package main.entity;

import javax.persistence.*;
import lombok.*;
import org.hibernate.annotations.Type;

/**
 * Сущность «Файл».
 */
@Entity
@Table(name = "files")
@Getter
@Setter
@Builder
@AllArgsConstructor
@NoArgsConstructor
public class File {
    /**
     * Внутренний идентификатор файла.
     */
    @Id
    @GeneratedValue(strategy = GenerationType.SEQUENCE)
    @Column(name = "id")
    private Long id;

    /**
     * Имя файла.
     */
    @Column(name = "name")
    private String name;

    /**
     * Содержимое файла, в байтах.
     */
    @Column(name = "content")
    @Lob
    @Type(type = "org.hibernate.type.BinaryType")
    private byte[] content;

    /**
     * Идентификатор пользователя-владельца файла.
     */
    @Column(name = "user_id")
    private Long userId;
}
