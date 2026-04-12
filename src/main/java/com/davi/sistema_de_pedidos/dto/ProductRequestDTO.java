package com.davi.sistema_de_pedidos.dto;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Positive;

import java.math.BigDecimal;

public record ProductRequestDTO(
        @NotBlank(message = "Campo nome precisa ser preenchido")
        String name,
        @NotNull(message = "Campo preço precisa ser preenchido")
        @Positive(message = "Preço precisa ser maior do que zero")
        BigDecimal price
) {
}
