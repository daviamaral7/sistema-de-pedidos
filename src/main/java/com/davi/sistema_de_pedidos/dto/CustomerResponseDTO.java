package com.davi.sistema_de_pedidos.dto;

import java.util.UUID;

public record CustomerResponseDTO(
        UUID id,
        String name,
        String email
) {
}
