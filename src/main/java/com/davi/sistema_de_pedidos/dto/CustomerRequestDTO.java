package com.davi.sistema_de_pedidos.dto;

import jakarta.validation.constraints.Email;
import jakarta.validation.constraints.NotBlank;

public record CustomerRequestDTO(
        @NotBlank(message = "Name is required")
        String name,
        @Email(message = "Email not valid")
        @NotBlank(message = "Email is required")
        String email
) {
}
