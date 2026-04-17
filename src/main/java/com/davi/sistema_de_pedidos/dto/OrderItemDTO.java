package com.davi.sistema_de_pedidos.dto;

import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Positive;

import java.util.UUID;

public record OrderItemDTO(
        @NotNull(message = "Product id is required")
        UUID productId,
        @Positive(message = "Quantity must be greater than 0")
        int quantity
) {
}
