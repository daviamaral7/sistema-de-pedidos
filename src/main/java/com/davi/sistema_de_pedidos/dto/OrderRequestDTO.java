package com.davi.sistema_de_pedidos.dto;

import jakarta.validation.Valid;
import jakarta.validation.constraints.NotEmpty;
import jakarta.validation.constraints.NotNull;

import java.util.List;
import java.util.UUID;

public record OrderRequestDTO(
        @NotNull(message = "Customer id is required")
        UUID customerId,
        @NotEmpty(message = "At least 1 item is required")
        @Valid
        List<OrderItemDTO> items
) {
}
