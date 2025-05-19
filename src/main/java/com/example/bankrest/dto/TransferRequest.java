package com.example.bankrest.dto;

import jakarta.validation.constraints.DecimalMin;
import jakarta.validation.constraints.NotNull;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

import java.math.BigDecimal;

@Setter
@Getter
@NoArgsConstructor
public class TransferRequest {

    @NotNull(message = "ID карты-отправителя обязателен")
    private Long fromCardId;

    @NotNull(message = "ID карты-получателя обязателен")
    private Long toCardId;

    @NotNull(message = "Сумма перевода обязателен")
    @DecimalMin(value = "0.01", message = "Сумма перевода должна быть не менее 0.01")
    private BigDecimal amount;

}