package com.example.bankrest.dto;

import jakarta.validation.constraints.NotBlank;
import lombok.Getter;
import lombok.Setter;

@Setter
@Getter
public class RoleRequest {
    @NotBlank(message = "Новую роль надо указать")
    private String role;
}
