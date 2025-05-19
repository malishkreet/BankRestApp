package com.example.bankrest.dto;

import io.swagger.v3.oas.annotations.media.Schema;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;
import lombok.Setter;

import java.time.LocalDateTime;

@Setter
@Getter
@AllArgsConstructor
@Builder
public class UserDetailsDto {
    @Schema(description = "ID пользователя", accessMode = Schema.AccessMode.READ_ONLY)
    private Long id;

    @Schema(description = "Логин пользователя", accessMode = Schema.AccessMode.READ_ONLY)
    private String username;

    @Schema(description = "Роль пользователя", accessMode = Schema.AccessMode.READ_ONLY)
    private String role;

    @Schema(description = "Дата и время создания аккаунта", accessMode = Schema.AccessMode.READ_ONLY)
    private LocalDateTime createdAt;
}
