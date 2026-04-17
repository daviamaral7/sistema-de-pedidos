package com.davi.sistema_de_pedidos.dto;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.List;
import java.util.UUID;

public record OrderResponseDTO(
        UUID orderId,
        CustomerSummaryDTO customer,
        LocalDateTime orderDate,
        String orderStatus,
        BigDecimal total,
        List<OrderItemResponseDTO> purchasedItems
) {
}
