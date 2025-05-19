package com.example.bankrest.dto;

import com.fasterxml.jackson.annotation.JsonProperty;
import jakarta.validation.constraints.*;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

import java.math.BigDecimal;
import java.time.LocalDate;

@Setter
@Getter
@NoArgsConstructor
public class CardDto {
    private Long id;

    @NotNull(message = "Для создания карты нужно передать ownerId")
    private Long ownerId;

    @NotBlank(message = "Номер карты обязателен")
    @Pattern(regexp = "\\d{16}", message = "Номер карты должен состоять из 16 цифр без пробелов")
    @JsonProperty(access = JsonProperty.Access.READ_WRITE)
    private String rawNumber;

    private String maskedNumber; // **** **** **** 1234
    private String ownerName;

    @NotNull(message = "Срок действия обязателен")
    private LocalDate expiryDate;

    private String status;

    @NotNull(message = "Баланс обязателен")
    @DecimalMin(value = "0.00", inclusive = true, message = "Баланс не может быть отрицательным")
    private BigDecimal balance;


}
