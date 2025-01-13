package main.service;

import lombok.AllArgsConstructor;
import lombok.RequiredArgsConstructor;
import main.dto.response.FileResponse;
import main.entity.File;
import main.repository.FileRepository;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.web.multipart.MultipartFile;

import javax.annotation.PostConstruct;
import javax.transaction.Transactional;
import java.io.IOException;
import java.net.InetAddress;
import java.net.UnknownHostException;
import java.util.List;
import java.util.Optional;
import java.util.stream.Collectors;

@Service
@AllArgsConstructor
public class FileService {
    @Autowired
    private FileRepository fileRepository;
    private final Logger logger = LoggerFactory.getLogger(FileService.class);

    /**
     * Добавляет файл в БД.
     * @param userId идентификатор пользователя
     * @param name название файла
     * @param file объект файла с содержимым
     * @throws IOException
     */
    public void addFile(Long userId, String name, MultipartFile file) throws IOException {
        File entity = File.builder()
                .name(name)
                .content(file.getBytes())
                .userId(userId)
                .build();
        fileRepository.save(entity);
        logger.info("Сохранён в БД файл с именем " + name + " и пользователем id = " + userId + ".");
    }

    /**
     * Удаляет файл из БД.
     * @param userId идентификатор пользователя
     * @param name название файла
     */
    public void deleteFile(Long userId, String name) {
        Optional<File> file = fileRepository.findByUserIdAndName(userId, name);
        if (file.isPresent()) {
            fileRepository.delete(file.get());
            logger.info("Удалён из БД файл с именем " + name + " и пользователем id = " + userId + ".");
        } else {
            logger.error("Файл с названием " + name + " не был добавлен у пользователя id = " + userId + "!");
            throw new IllegalArgumentException("Файл с названием " + name + " не был добавлен у пользователя id = " + userId + "!");
        }
    }

    /**
     * Изменяет название файла.
     * @param userId идентификатор пользователя
     * @param name старое имя файла
     * @param newName новое имя файла
     * @throws IllegalArgumentException если файл с данным именем не был найден у пользователя
     */
    @Transactional
    public void editFile(Long userId, String name, String newName) {
        Optional<File> file = fileRepository.findByUserIdAndName(userId, name);
        if (file.isEmpty()) {
            logger.error("Файл с названием " + name + " не был найден у пользователя id = " + userId + "!");
            throw new IllegalArgumentException("Файл с названием " + name + " не был найден у пользователя id = " + userId + "!");
        }
        File newFile = file.get();
        newFile.setName(newName);
        fileRepository.save(newFile);
        logger.info("Файл с названием " + name + " был переименован на " + newName + " у пользователя id = " + userId + ".");
    }

    /**
     * Получает содержимое файла.
     * @param userId идентификатор пользователя
     * @param name имя
     * @return содержимое файла, в байтах
     * @throws IllegalArgumentException если файл с данным именем не был найден у пользователя
     */
    @Transactional
    public byte[] getFile(Long userId, String name) {
        Optional<File> file = fileRepository.findByUserIdAndName(userId, name);
        if (file.isEmpty()) {
            logger.error("Попытка получения несуществующего у пользователя id = " + userId + " файла с именем " + name);
            throw new IllegalArgumentException("Файл с названием " + name + " не был найден!");
        }
        return file.get().getContent();
    }

    /**
     * Получение списка файлов у пользователя.
     * @param userId идентификатор пользователя
     * @param limit максимальное кол-во файлов, которые нужно вернуть
     * @return список файлов данного пользователя
     */
    @Transactional
    public List<FileResponse> getFileList(Long userId, int limit) {
        return fileRepository.findAllByUserId(userId)
                .stream()
                .limit(limit)
                .map(file -> new FileResponse(file.getName(), file.getContent().length))
                .collect(Collectors.toList());
    }
}
