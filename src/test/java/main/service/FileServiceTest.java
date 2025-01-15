package main.service;

import main.dto.response.FileResponse;
import main.entity.File;
import main.repository.FileRepository;
import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.orm.jpa.DataJpaTest;
import org.springframework.http.MediaType;
import org.springframework.mock.web.MockMultipartFile;
import org.springframework.test.context.TestPropertySource;
import org.springframework.test.context.junit.jupiter.SpringExtension;

import java.io.IOException;
import java.util.Arrays;
import java.util.Collection;
import java.util.List;

@DataJpaTest
@ExtendWith(SpringExtension.class)
@TestPropertySource(locations = "classpath:application-test.properties")
public class FileServiceTest {
    @Autowired
    private FileRepository fileRepository;

    private FileService fileService;

    @BeforeEach
    public void beforeEach() {
        fileService = new FileService(fileRepository);

        fileRepository.deleteAll();
    }

    @Test
    @DisplayName("Добавление файла в БД")
    public void addFile() throws IOException {
        byte[] bytes = "Какой-то текст".getBytes();
        MockMultipartFile file = new MockMultipartFile(
                "file",
                "file.txt",
                MediaType.TEXT_PLAIN_VALUE,
                bytes
        );

        fileService.addFile(1L, "file.txt", file);

        List<File> files = fileRepository.findAll();
        Assertions.assertEquals(1, files.size());
        File f = files.get(0);
        Assertions.assertEquals(1L, f.getUserId());
        Assertions.assertEquals("file.txt", f.getName());
        Assertions.assertTrue(Arrays.equals(bytes, f.getContent()));
    }

    @Test
    @DisplayName("Удаление файла из БД")
    public void deleteFile() {
        File file = new File(
                null,
                "file.txt",
                new byte[]{0, 1, 2, 3},
                1L
        );
        fileRepository.save(file);

        // Попытка удаления файла с неправильным userId или названием
        Assertions.assertThrows(IllegalArgumentException.class, () -> {
            fileService.deleteFile(2L, "file.txt");
        });

        Assertions.assertThrows(IllegalArgumentException.class, () -> {
            fileService.deleteFile(1L, "other.txt");
        });

        fileService.deleteFile(1L, "file.txt");
        Assertions.assertTrue(fileRepository.findAll().isEmpty());
    }

    @Test
    @DisplayName("Переименование файла")
    public void editFile() {
        File file = new File(
                null,
                "file.txt",
                new byte[]{0, 1, 2, 3},
                1L
        );
        fileRepository.save(file);

        fileService.editFile(1L, "file.txt", "new.txt");

        File f = fileRepository.findAll().get(0);
        Assertions.assertEquals("new.txt", f.getName());
    }

    @Test
    @DisplayName("Получение содержимого файла")
    public void getFile() {
        File file = new File(
                null,
                "file.txt",
                new byte[]{0, 1, 2, 3},
                1L
        );
        fileRepository.save(file);

        Assertions.assertTrue(Arrays.equals(
                new byte[]{0, 1, 2, 3},
                fileService.getFile(1L, "file.txt")
        ));
    }

    @Test
    @DisplayName("Получение списка файлов")
    public void getFileList() {
        File file = new File(
                null,
                "file.txt",
                new byte[]{0, 1, 2, 3},
                1L
        );
        fileRepository.save(file);

        Collection<File> files = fileService.getFileList(1L, 1);
        Assertions.assertEquals(1, files.size());
        File f = files.iterator().next();
        Assertions.assertEquals(4, f.getContent().length);
        Assertions.assertEquals("file.txt", file.getName());
    }
}
