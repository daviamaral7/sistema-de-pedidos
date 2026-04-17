package com.davi.sistema_de_pedidos.dto;

import java.math.BigDecimal;
import java.util.UUID;

public record OrderItemResponseDTO(
        UUID productId,
        String productName,
        int quantity,
        BigDecimal priceAtPurchase
) {
}
