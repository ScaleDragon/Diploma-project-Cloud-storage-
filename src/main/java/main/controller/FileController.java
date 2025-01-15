package main.controller;

import main.dto.request.EditFileNameRequest;
import main.dto.response.FileResponse;
import main.service.FileService;
import main.service.SessionService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;

import java.io.IOException;
import java.util.List;
import java.util.stream.Collectors;

/**
 * REST-контроллер для управления файлами.
 */
@RestController
@RequestMapping
public class FileController {
    @Autowired
    private FileService fileService;
    @Autowired
    private SessionService sessionService;

    @PostMapping("/file")
    public void addFile(
            @RequestHeader("auth-token") String authToken,
            @RequestParam(name = "filename") String name,
            @RequestBody MultipartFile file
    ) throws IOException {
        authToken = authToken.split(" ")[1];
        Long userId = sessionService.checkAuthToken(authToken);
        fileService.addFile(userId, name, file);
    }

    @DeleteMapping("/file")
    public void deleteFile(
            @RequestHeader("auth-token") String authToken,
            @RequestParam(name = "filename") String name
    ) {
        authToken = authToken.split(" ")[1];
        Long userId = sessionService.checkAuthToken(authToken);
        fileService.deleteFile(userId, name);
    }

    @PutMapping("/file")
    public void editFile(
            @RequestHeader("auth-token") String authToken,
            @RequestParam(name = "filename") String name,
            @RequestBody EditFileNameRequest editRequest
    ) {
        authToken = authToken.split(" ")[1];
        Long userId = sessionService.checkAuthToken(authToken);
        fileService.editFile(userId, name, editRequest.getFilename());
    }

    @GetMapping("/file")
    public ResponseEntity<byte[]> downloadFile(
            @RequestHeader("auth-token") String authToken,
            @RequestParam(name = "filename") String name
    ) {
        authToken = authToken.split(" ")[1];
        Long userId = sessionService.checkAuthToken(authToken);
        return ResponseEntity.ok(fileService.getFile(userId, name));
    }

    @GetMapping("/list")
    public List<FileResponse> getFileList(
            @RequestHeader("auth-token") String authToken,
            @RequestParam(name = "limit") Integer limit
    ) {
        authToken = authToken.split(" ")[1];
        Long userId = sessionService.checkAuthToken(authToken);
        return fileService.getFileList(userId, limit).stream()
                .map(file -> new FileResponse(file.getName(), file.getContent().length))
                .collect(Collectors.toList());
    }
}
