package com.davi.sistema_de_pedidos.repository;

import com.davi.sistema_de_pedidos.model.Customer;
import com.davi.sistema_de_pedidos.model.Order;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;
import java.util.UUID;

public interface OrderRepository extends JpaRepository<Order, UUID> {

    List<Order> findByCustomer(Customer customer);

}
