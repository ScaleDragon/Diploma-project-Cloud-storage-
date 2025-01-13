package main.controller;

import lombok.RequiredArgsConstructor;
import main.dto.request.LoginRequest;
import main.dto.response.LoginResponse;
import main.exception.AuthorizationException;
import main.service.UserService;
import org.apache.coyote.Response;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.security.NoSuchAlgorithmException;
import java.util.Map;

@RestController
@RequestMapping
public class UserController {
    @Autowired
    private UserService userService;

    @PostMapping("/login")
    public ResponseEntity<LoginResponse> login(@RequestBody LoginRequest loginRequest) {
        try {
            return ResponseEntity.ok(userService.login(loginRequest));
        } catch (AuthorizationException exception) {
            return ResponseEntity.badRequest().body(new LoginResponse());
        } catch (Exception exception) {
            return ResponseEntity.internalServerError().body(new LoginResponse());
        }
    }

    @PostMapping("/logout")
    public ResponseEntity<Boolean> logout(@RequestHeader("auth-token") String authToken) {
        authToken = authToken.split(" ")[1];
        userService.logout(authToken);
        return ResponseEntity.ok(true);
    }

    @PostMapping("/register")
    public ResponseEntity<Boolean> register(
            @RequestParam(name = "login") String login,
            @RequestParam(name = "password") String password
    ) throws NoSuchAlgorithmException {
        userService.addUser(login, password);
        return ResponseEntity.ok(true);
    }
}
