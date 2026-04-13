package com.davi.sistema_de_pedidos.repository;

import com.davi.sistema_de_pedidos.model.Customer;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.UUID;

public interface CustomerRepository extends JpaRepository<Customer, UUID> {
}
