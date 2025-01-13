package main.entity;

import javax.persistence.*;
import lombok.*;
import org.hibernate.annotations.Type;

/**
 * Сущность «Пользователь».
 */
@Entity
@Table(name = "users")
@Getter
@Setter
@Builder
@AllArgsConstructor
@NoArgsConstructor
public class User {
    /**
     * Внутренний идентификатор пользователя.
     */
    @Id
    @Column(name = "id")
    @GeneratedValue(strategy = GenerationType.SEQUENCE)
    private Long id;

    /**
     * Логин пользователя.
     */
    @Column(name = "login")
    private String login;

    /**
     * Пароль, захешированный алгоритмом SHA-512 с солью.
     */
    @Lob
    @Type(type = "org.hibernate.type.BinaryType")
    @Column(name = "password")
    private byte[] password;
}
