package com.davi.sistema_de_pedidos.exceptions;

public class OrderStatusException extends RuntimeException {
    public OrderStatusException(String message) {
        super(message);
    }
}
