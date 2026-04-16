package com.davi.sistema_de_pedidos.repository;

import com.davi.sistema_de_pedidos.model.OrderItem;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.UUID;

public interface OrderItemRepository extends JpaRepository<OrderItem, UUID> {
}
