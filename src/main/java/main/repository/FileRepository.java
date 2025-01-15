package main.repository;

import main.entity.File;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.Collection;
import java.util.Optional;

@Repository
public interface FileRepository extends JpaRepository<File, Long> {
    /**
     * Получает список файлов по идентификатору пользователя.
     * @param userId идентификатор пользователя
     * @param pageRequest детали о количестве возвращаемых записей
     * @return список всех файлов пользователя
     */
    Collection<File> findAllByUserId(Long userId, PageRequest pageRequest);

    /**
     * Получает файл, при наличии, по названию и идентификатору пользователя.
     * @param userId идентификатор пользователя
     * @param name наименование файла
     * @return указанный файл, при наличии
     */
    Optional<File> findByUserIdAndName(Long userId, String name);
}
