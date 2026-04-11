package com.davi.sistema_de_pedidos.repository;

import com.davi.sistema_de_pedidos.model.Product;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.UUID;

public interface ProductRepository extends JpaRepository<Product, UUID> {
}
