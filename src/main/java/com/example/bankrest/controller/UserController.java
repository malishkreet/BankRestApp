package com.example.bankrest.controller;

import com.example.bankrest.dto.RoleRequest;
import com.example.bankrest.dto.UserDetailsDto;
import com.example.bankrest.dto.UserDto;
import com.example.bankrest.service.UserService;
import io.swagger.v3.oas.annotations.Operation;
import jakarta.validation.Valid;
import org.springdoc.core.annotations.ParameterObject;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import org.springframework.security.access.AccessDeniedException;
import java.security.Principal;

@RestController
@RequestMapping("/users")
public class UserController {
    private final UserService service;
    public UserController(UserService service) { this.service = service; }
    @Operation(summary = "Добавление пользователя (Только Админ)")
    @PostMapping
    public ResponseEntity<UserDto> register(@RequestBody UserDto dto) {
        UserDto created = service.createUser(dto.getUsername(), dto.getPassword(), "USER");
        return ResponseEntity.ok(created);
    }

    @PatchMapping("/{id}/role")
    @Operation(summary = "Сменить роль пользователя (Только ADMIN)")
    public ResponseEntity<Void> changeRole(
            @PathVariable Long id,
            @Valid @RequestBody RoleRequest req
    ) {
        service.updateUserRole(id, req.getRole());
        return ResponseEntity.noContent().build();
    }

    @GetMapping
    @Operation(summary = "Список всех пользователей (только ADMIN)")
    public ResponseEntity<Page<UserDto>> listUsers(
            @RequestParam(required = false) String usernamePart,
            @ParameterObject Pageable pageable
    ) {
        Page<UserDto> page;
        if (usernamePart != null && !usernamePart.isBlank()) {
            page = service.findUsersByUsernameContaining(usernamePart, pageable);
        } else {
            page = service.listUsers(pageable);
        }
        return ResponseEntity.ok(page);
    }
    @GetMapping("/{id}")
    @Operation(summary = "Детали пользователя по ID (только ADMIN)")
    public ResponseEntity<UserDetailsDto> getUserDetails(@PathVariable Long id) {
        UserDetailsDto dto = service.getUserDetailsById(id);
        return ResponseEntity.ok(dto);
    }
    @Operation(summary = "Удаление пользователя (Только ADMIN)")
    @DeleteMapping("{id}")
    public ResponseEntity<Void> deleteUser(
            @PathVariable Long id,
            Principal principal
    ) {
        UserDto me = service.findByUsername(principal.getName());

        if (me.getId().equals(id)) {
            throw new AccessDeniedException("Нельзя удалить свой собственный аккаунт");
        }
        if (id.equals(1L)) {
            throw new AccessDeniedException("Нельзя удалить главного админа");
        }

        service.deleteUser(id);
        return ResponseEntity.noContent().build();
    }
}
