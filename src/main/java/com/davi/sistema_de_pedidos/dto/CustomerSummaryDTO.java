package com.davi.sistema_de_pedidos.dto;

import java.util.UUID;

public record CustomerSummaryDTO(
        UUID id,
        String name
) {
}
