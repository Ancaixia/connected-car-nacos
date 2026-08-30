package com.example.connectedcar.web;

import com.example.connectedcar.domain.User;
import com.example.connectedcar.service.AuthService;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestHeader;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.util.HashMap;
import java.util.Map;

@RestController
@RequestMapping("/api/auth")
public class AuthController {

    private final AuthService authService;

    public AuthController(AuthService authService) {
        this.authService = authService;
    }

    @PostMapping("/login")
    public ResponseEntity<Map<String, Object>> login(@RequestBody LoginRequest req) {
        if (req == null || req.username == null || req.password == null) {
            return bad("用户名或密码为空");
        }
        String token = authService.login(req.username.trim(), req.password);
        if (token == null) {
            return ResponseEntity.status(HttpStatus.UNAUTHORIZED)
                    .body(Map.of("success", false, "message", "用户名或密码错误"));
        }
        User user = authService.verify(token);
        Map<String, Object> body = new HashMap<>();
        body.put("success", true);
        body.put("token", token);
        body.put("user", user);
        return ResponseEntity.ok(body);
    }

    @GetMapping("/check")
    public ResponseEntity<Map<String, Object>> check(
            @RequestHeader(value = "Authorization", required = false) String authorization) {
        String token = bearer(authorization);
        User user = authService.verify(token);
        if (user == null) {
            return ResponseEntity.status(HttpStatus.UNAUTHORIZED)
                    .body(Map.of("success", false, "message", "未登录或会话已过期"));
        }
        return ResponseEntity.ok(Map.of("success", true, "user", user));
    }

    @GetMapping("/me")
    public ResponseEntity<Map<String, Object>> me(
            @RequestHeader(value = "Authorization", required = false) String authorization) {
        String token = bearer(authorization);
        User user = authService.verify(token);
        if (user == null) {
            return ResponseEntity.status(HttpStatus.UNAUTHORIZED)
                    .body(Map.of("success", false, "message", "未登录或会话已过期"));
        }
        return ResponseEntity.ok(Map.of("success", true, "user", user));
    }

    @PostMapping("/logout")
    public ResponseEntity<Map<String, Object>> logout(
            @RequestHeader(value = "Authorization", required = false) String authorization) {
        String token = bearer(authorization);
        authService.logout(token);
        return ResponseEntity.ok(Map.of("success", true));
    }

    private String bearer(String authorization) {
        if (authorization == null) return null;
        if (authorization.startsWith("Bearer ")) {
            return authorization.substring(7);
        }
        return authorization;
    }

    private ResponseEntity<Map<String, Object>> bad(String message) {
        return ResponseEntity.badRequest().body(Map.of("success", false, "message", message));
    }

    public static final class LoginRequest {
        public String username;
        public String password;
    }
}
