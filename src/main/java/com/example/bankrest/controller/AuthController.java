package com.example.bankrest.controller;

import com.example.bankrest.dto.UserDto;
import com.example.bankrest.security.JwtTokenProvider;
import com.example.bankrest.service.UserService;
import io.swagger.v3.oas.annotations.Operation;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.AuthenticationException;
import org.springframework.web.bind.annotation.*;

import jakarta.validation.Valid;
import java.util.Map;

@RestController
@RequestMapping("/auth")
public class AuthController {

    private final AuthenticationManager authManager;
    private final JwtTokenProvider tokenProvider;
    private final UserService userService;

    public AuthController(AuthenticationManager authManager,
                          JwtTokenProvider tokenProvider,
                          UserService userService) {
        this.authManager   = authManager;
        this.tokenProvider = tokenProvider;
        this.userService   = userService;
    }

    @Operation(
            summary = "Регистрация нового пользователя",
            security = {}
    )
    @PostMapping("/register")
    public ResponseEntity<UserDto> register(@Valid @RequestBody UserDto dto) {
        UserDto created = userService.createUser(
                dto.getUsername(),
                dto.getPassword(),
                "USER"
        );
        return ResponseEntity
                .status(HttpStatus.CREATED)
                .body(created);
    }

    @Operation(
            summary = "Логин и получение JWT",
            security = {}
    )
    @PostMapping("/login")
    public ResponseEntity<Map<String, String>> login(@Valid @RequestBody UserDto dto) {
        try {
            Authentication auth = authManager.authenticate(
                    new UsernamePasswordAuthenticationToken(
                            dto.getUsername(),
                            dto.getPassword()
                    )
            );
            String grantedRole = auth.getAuthorities()
                    .iterator()
                    .next()
                    .getAuthority()
                    .substring(5);
            String token = tokenProvider.createToken(dto.getUsername(), grantedRole);

            return ResponseEntity.ok(Map.of("token", token));
        } catch (AuthenticationException ex) {
            return ResponseEntity
                    .status(HttpStatus.UNAUTHORIZED)
                    .body(Map.of("error", "Invalid credentials"));
        }
    }
}
